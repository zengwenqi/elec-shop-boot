package elec.shop.security;

import elec.shop.utils.AllContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null || !(permission instanceof String)) {
            return false;
        }

        Long userId = AllContextUtils.getLoginSysUser().getUserId();
        String permissionKey = "user:permissions:" + userId;

        try {
            // 从Redis中获取用户权限列表
            List<String> userPermissions = (List<String>) redisTemplate.opsForValue().get(permissionKey);
            if (userPermissions == null) {
                log.warn("No permissions found in Redis for user: {}", userId);
                return false;
            }

            String requiredPermission = permission.toString();
            log.debug("Checking permission: {} for user: {}", requiredPermission, userId);
            log.debug("User permissions: {}", userPermissions);

            // 如果用户拥有superadmin权限，直接返回true
            if (userPermissions.contains("superadmin")) {
                return true;
            }

            // 检查是否包含所需权限
            return userPermissions.contains(requiredPermission);
        } catch (Exception e) {
            log.error("Error checking permissions for user: " + userId, e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType, permission);
    }
}
