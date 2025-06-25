package elec.shop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.config.JwtConfig;
import elec.shop.utils.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenManager tokenManager;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    private static final int TOKEN_EXPIRED = 401001; // token过期
    private static final int TOKEN_INVALID = 401002; // token无效

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            // 1. 获取请求路径
            String requestPath = request.getRequestURI();

            // 2. 如果是白名单路径，直接放行
            if (isPermitAllRequest(requestPath)) {
                chain.doFilter(request, response);
                return;
            }

            // 3. 获取并验证Access Token
            String authHeader = request.getHeader(jwtConfig.getIssuer());
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, TOKEN_INVALID, "没有权限");
                return;
            }

            String accessToken = authHeader.substring(7);
            String username = null;

            try {
                username = tokenManager.extractClaim(accessToken, Claims::getSubject);
                // 验证token是否在黑名单中
                if (tokenManager.isTokenBlacklisted(accessToken)) {
                    sendErrorResponse(response, TOKEN_INVALID, "没有权限");
                    return;
                }
            } catch (ExpiredJwtException e) {
                // Access Token过期，尝试使用Refresh Token
                handleExpiredAccessToken(request, response, chain);
                return;
            } catch (Exception e) {
                sendErrorResponse(response, TOKEN_INVALID, "没有权限");
                return;
            }

            // 4. 设置认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (tokenManager.validateAccessToken(accessToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    sendErrorResponse(response, TOKEN_INVALID, "Token验证失败");
                    return;
                }
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("认证过程中发生错误: {}", e.getMessage(), e);
            sendErrorResponse(response, TOKEN_INVALID, "认证失败");
        } finally {
            // 5. 清理SecurityContext
            SecurityContextHolder.clearContext();
        }
    }

    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> refreshTokenCookie = Arrays.stream(cookies)
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst();

            if (refreshTokenCookie.isPresent()) {
                String refreshToken = refreshTokenCookie.get().getValue();
                try {
                    String username = tokenManager.extractClaim(refreshToken, Claims::getSubject);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (tokenManager.validateRefreshToken(refreshToken, userDetails)) {
                        String newAccessToken = tokenManager.generateAccessToken(userDetails);
                        response.setHeader(jwtConfig.getIssuer(), "Bearer " + newAccessToken);

                        // 设置新的认证信息
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 继续请求
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (Exception e) {
                    log.error("Refresh token处理失败: {}", e.getMessage());
                }
            }
        }
        sendErrorResponse(response, TOKEN_EXPIRED, "Token已过期，请重新登录");
    }

    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<?> result = Result.build(null, code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 检查请求是否在白名单中
     * @param requestPath 请求路径
     * @return 如果在白名单中返回true，否则返回false
     */
    private boolean isPermitAllRequest(String requestPath) {
        // 白名单路径列表
        final List<String> permitAllPaths = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/email/code",
            "/auth/email/verify",
            "/auth/reset-password",
            "/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/announcement",
            "/ws",
            "/ws/notice"
        );

        // 检查完整路径匹配
        if (permitAllPaths.contains(requestPath)) {
            return true;
        }

        // 检查路径前缀匹配
        return permitAllPaths.stream().anyMatch(path ->
            !path.equals("/") && requestPath.startsWith(path));
    }
}
