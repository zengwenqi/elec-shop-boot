package elec.shop.security;

import elec.shop.config.JwtConfig;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenManager {
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final String REFRESH_TOKEN_KEY = "refresh_token:";

    // 生成Access Token
    public String generateAccessToken(UserDetails userDetails) {
        // 生成新的access token
        String newToken = generateToken(userDetails, jwtConfig.getAccessTokenPrivateKey(), jwtConfig.getAccessTokenExpiration());
        // 可以在这里做一些额外的处理，比如记录token生成时间等
        return newToken;
    }

    // 生成Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, jwtConfig.getRefreshTokenPrivateKey(),
                jwtConfig.getRefreshTokenExpiration());
    }

    private String generateToken(UserDetails userDetails, String privateKeyStr, long expiration) {
        try {
            if (privateKeyStr == null || privateKeyStr.trim().isEmpty()) {
                log.error("私钥为空");
                throw new RuntimeException("Private key is not configured");
            }

            log.debug("开始生成token，用户：{}", userDetails.getUsername());

            // 移除可能存在的换行符
            privateKeyStr = privateKeyStr.replaceAll("\\s+", "");

            Map<String, Object> claims = new HashMap<>();
            if (userDetails instanceof CustomUserDetails) {
                claims.put("userId", ((CustomUserDetails) userDetails).getUserId());
            }
            claims.put("jti", UUID.randomUUID().toString());

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .setIssuer(jwtConfig.getIssuer())
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();

            log.debug("Token生成成功");
            return token;
        } catch (Exception e) {
            log.error("Token生成失败：{}", e.getMessage(), e);
            throw new RuntimeException("Token generation failed: " + e.getMessage(), e);
        }
    }

    // 验证Access Token
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails, jwtConfig.getAccessTokenPublicKey());
    }

    // 验证Refresh Token
    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        // 验证refresh token是否在Redis中存在且匹配
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + userDetails.getUsername());
        if (!token.equals(storedToken)) {
            return false;
        }
        return validateToken(token, userDetails, jwtConfig.getRefreshTokenPublicKey());
    }

    private boolean validateToken(String token, UserDetails userDetails, String publicKeyStr) {
        try {
            if (publicKeyStr == null || publicKeyStr.trim().isEmpty()) {
                log.error("公钥为空");
                return false;
            }

            // 检查token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                log.debug("Token在黑名单中");
                return false;
            }

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            boolean isValid = username.equals(userDetails.getUsername())
                    && expiration.after(new Date());

            if (!isValid) {
                log.debug("Token验证失败：username={}, expired={}",
                    username.equals(userDetails.getUsername()),
                    expiration.before(new Date()));
            }

            return isValid;
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期");
            return false;
        } catch (Exception e) {
            log.error("Token验证失败：{}", e.getMessage());
            return false;
        }
    }

    // 将token加入黑名单
    public void blacklistToken(String token) {
        try {
            String jti = extractClaim(token, Claims::getId);
            redisTemplate.opsForValue().set(
                TOKEN_BLACKLIST_PREFIX + jti,
                "blacklisted",
                jwtConfig.getAccessTokenExpiration(),
                TimeUnit.MILLISECONDS
            );
            log.debug("Token已加入黑名单：{}", jti);
        } catch (Exception e) {
            log.error("添加Token到黑名单失败：{}", e.getMessage());
        }
    }

    // 检查token是否在黑名单中
    public boolean isTokenBlacklisted(String token) {
        try {
            String jti = extractClaim(token, Claims::getId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + jti));
        } catch (ExpiredJwtException e) {
            // 过期Token无需检查黑名单，直接返回false
            log.debug("Token已过期，无需检查黑名单");
            return false;
        } catch (Exception e) {
            log.error("检查Token黑名单失败：{}", e.getMessage());
            return true; // 其他异常时返回true（安全起见，默认视为无效）
        }
    }

    // 记录登录失败次数
    public void recordLoginAttempt(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, jwtConfig.getLoginLockDuration(), TimeUnit.SECONDS);
        log.debug("记录登录失败：{}", username);
    }

    // 检查是否超过登录尝试次数限制
    public boolean isLoginLocked(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(key);
        boolean isLocked = attempts != null && Integer.parseInt(attempts) >= jwtConfig.getLoginAttemptLimit();
        if (isLocked) {
            log.debug("用户已被锁定：{}", username);
        }
        return isLocked;
    }

    // 重置登录尝试次数
    public void resetLoginAttempts(String username) {
        redisTemplate.delete(LOGIN_ATTEMPT_PREFIX + username);
        log.debug("重置登录尝试次数：{}", username);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            // 从token中获取issuer，确定是access token还是refresh token
            String tokenWithoutSignature = token.substring(0, token.lastIndexOf('.') + 1);
            Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
            Claims claims = untrusted.getBody();

            // 根据token过期时间判断类型，选择公钥
            String publicKeyStr;
            if (claims.getExpiration().getTime() - claims.getIssuedAt().getTime() > jwtConfig.getAccessTokenExpiration()) {
                publicKeyStr = jwtConfig.getRefreshTokenPublicKey();
                log.debug("使用Refresh Token公钥验证");
            } else {
                publicKeyStr = jwtConfig.getAccessTokenPublicKey();
                log.debug("使用Access Token公钥验证");
            }

            if (publicKeyStr == null || publicKeyStr.trim().isEmpty()) {
                log.error("公钥为空");
                throw new RuntimeException("Public key is not configured");
            }

            publicKeyStr = publicKeyStr.replaceAll("\\s+", ""); // 移除换行符
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            return Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 单独捕获Token过期异常，直接抛出（不转换为RuntimeException）
            log.debug("Token已过期（extractAllClaims）");
            throw e;
        } catch (Exception e) {
            log.error("Token解析失败（非过期原因）：{}", e.getMessage());
            throw new RuntimeException("Failed to extract claims: " + e.getMessage(), e);
        }
    }

    public void storeRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_KEY + username;
        redisTemplate.opsForValue().set(key, refreshToken,
            jwtConfig.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);
    }

    public boolean validateStoredRefreshToken(String username, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + username);
        return refreshToken.equals(storedToken);
    }

    public void invalidateRefreshToken(String username) {
        redisTemplate.delete(REFRESH_TOKEN_KEY + username);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean canRefreshToken(String username) {
        String refreshTokenKey = REFRESH_TOKEN_KEY + username;
        // 检查refresh token是否存在且未过期
        return Boolean.TRUE.equals(redisTemplate.hasKey(refreshTokenKey));
    }
}
