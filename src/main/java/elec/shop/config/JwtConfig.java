package elec.shop.config;

import elec.shop.service.sys.JwtConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@RequiredArgsConstructor
public class JwtConfig implements InitializingBean {
    private final JwtConfigService jwtConfigService;

    private String accessTokenPrivateKey;
    private String accessTokenPublicKey;
    private String refreshTokenPrivateKey;
    private String refreshTokenPublicKey;
    private long accessTokenExpiration = 900000; // 15分钟
    private long refreshTokenExpiration = 604800000; // 7天
    private String issuer = "elec-shop-token";
    private int loginAttemptLimit = 5; // 登录尝试次数限制
    private int loginLockDuration = 300; // 登录锁定时间（秒）

    @Override
    public void afterPropertiesSet() {
        log.info("开始加载JWT配置...");
        try {
            // 从数据库加载配置
            this.accessTokenPrivateKey = getConfigOrDefault("jwt.access-token-private-key", this.accessTokenPrivateKey);
            this.accessTokenPublicKey = getConfigOrDefault("jwt.access-token-public-key", this.accessTokenPublicKey);
            this.refreshTokenPrivateKey = getConfigOrDefault("jwt.refresh-token-private-key", this.refreshTokenPrivateKey);
            this.refreshTokenPublicKey = getConfigOrDefault("jwt.refresh-token-public-key", this.refreshTokenPublicKey);
            this.accessTokenExpiration = Long.parseLong(getConfigOrDefault("jwt.access-token-expiration", String.valueOf(this.accessTokenExpiration)));
            this.refreshTokenExpiration = Long.parseLong(getConfigOrDefault("jwt.refresh-token-expiration", String.valueOf(this.refreshTokenExpiration)));
            this.issuer = getConfigOrDefault("jwt.issuer", this.issuer);
            this.loginAttemptLimit = Integer.parseInt(getConfigOrDefault("jwt.login-attempt-limit", String.valueOf(this.loginAttemptLimit)));
            this.loginLockDuration = Integer.parseInt(getConfigOrDefault("jwt.login-lock-duration", String.valueOf(this.loginLockDuration)));

            // 验证密钥格式
            validateKeyFormat(this.accessTokenPrivateKey, "Access Token私钥");
            validateKeyFormat(this.accessTokenPublicKey, "Access Token公钥");
            validateKeyFormat(this.refreshTokenPrivateKey, "Refresh Token私钥");
            validateKeyFormat(this.refreshTokenPublicKey, "Refresh Token公钥");
        } catch (Exception e) {
            log.error("JWT配置加载失败：{}", e.getMessage(), e);
            throw new RuntimeException("JWT配置加载失败", e);
        }
    }

    private void validateKeyFormat(String key, String keyName) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("{}为空", keyName);
            return;
        }

        try {
            // 尝试Base64解码
            byte[] decoded = java.util.Base64.getDecoder().decode(key);
            log.debug("{}格式正确，解码后长度: {}", keyName, decoded.length);
        } catch (IllegalArgumentException e) {
            log.error("{}格式错误: {}", keyName, e.getMessage());
            throw new RuntimeException(keyName + "格式错误: " + e.getMessage());
        }
    }

    private String getConfigOrDefault(String key, String defaultValue) {
        String value = jwtConfigService.getConfigValue(key);
        if (value == null) {
            log.warn("配置项 {} 未找到，使用默认值", key);
            return defaultValue;
        }
        log.debug("加载配置项：{} = {}", key, key.contains("key") ? "***" : value);
        return value;
    }
}
