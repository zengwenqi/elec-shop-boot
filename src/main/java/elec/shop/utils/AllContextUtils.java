package elec.shop.utils;

import elec.shop.pojo.sys.SysUser;
import elec.shop.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局通用工具类
 */
public class AllContextUtils {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 获取当前登录用户信息
     * @return
     */
    public static SysUser getLoginSysUser() {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SysUser sysUser = new SysUser();
        // 未认证或匿名用户处理
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            sysUser.setUsername("system");
            sysUser.setUserId(1L);
            return sysUser; // 未登录时使用默认用户（可配置为 null 或其他值）
        }
        String username = authentication != null ? authentication.getName() : null;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        sysUser.setUserId(userId);
        sysUser.setUsername(username);
        return sysUser;
    }


    /**
     * 生成唯一店铺编号（格式：US-用户ID-时间戳-序列号）
     * @param userId 用户ID
     * @return 唯一店铺编号（示例：US-123456-20231015143022-0001）
     */
    public static String generateUniqueShopNumber(Long userId) {
        // 获取当前时间戳（精确到秒）
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // 获取0-9999的循环序列号（解决同一毫秒内的并发问题）
        int sequence = COUNTER.getAndIncrement() % 10000;
        String seqStr = String.format("%04d", sequence);

        // 组合编号：前缀-用户ID-时间戳-序列号
        return String.format("US-%d-%s-%s", userId, timestamp, seqStr);
    }

}
