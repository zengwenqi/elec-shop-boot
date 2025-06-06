package elec.shop.utils;

import elec.shop.pojo.sys.SysUser;
import elec.shop.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局通用工具类
 */
public class AllContextUtils {

    // 原子计数器，用于生成序列号
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    // 使用 SecureRandom 生成安全随机数
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    // 计数器的最大值，超过后重置
    private static final int MAX_COUNTER = 999;
    // 每毫秒可生成的最大序列号（防止溢出）
    private static final int MAX_SEQUENCE = 9999;
    // 订单号前缀
    private static final String ORDER_PREFIX = "ORD";
    // 账户编号前缀
    private static final String PREFIX = "ACC";
    // 日期格式
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyMMddHHmmss"));

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


    /**
     * 生成唯一的账户编号
     * @param userId 用户ID
     * @return 唯一的账户编号
     */
    public static String generateAccountNo(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正整数");
        }

        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 格式化时间戳部分
        String datePart = DATE_FORMAT.get().format(new Date(timestamp));

        // 获取用户ID的后6位（不足6位补零）
        String userIdPart = String.format("%06d", userId % 1000000);

        // 获取随机数部分（3位数字）
        int randomNum = SECURE_RANDOM.nextInt(900) + 100; // 100-999

        // 获取计数器值（3位数字）
        int count = COUNTER.getAndIncrement();
        if (count >= MAX_COUNTER) {
            COUNTER.set(0); // 重置计数器
        }
        String counterPart = String.format("%03d", count);

        // 组合各部分生成完整的账户编号
        return String.format("%s%s%s%03d%s",
                PREFIX, datePart, userIdPart, randomNum, counterPart);
    }

    /**
     * 生成基于用户 ID 的 32 位唯一账户 ID
     * @param userId 用户唯一标识（建议使用 Long 类型）
     * @return 32 位十六进制字符串（大写）
     */
    public static String generateAccountId(Long userId) {
        try {
            // 1. 生成盐值（可配置为系统固定值或动态密钥）
            String salt = "elec-shop-token"; // 请替换为实际盐值

            // 2. 组合用户 ID、UUID、盐值
            String input = userId + "" + UUID.randomUUID() + salt;

            // 3. 使用 MD5 哈希生成 128 位二进制数据（16字节）
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            // 4. 转换为 32 位十六进制字符串（大写）
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b)); // 两位大写十六进制
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 生成唯一订单编号
     * @param userId 用户ID
     * @return 订单编号（格式：ORD+时间戳+用户ID+序列号+随机数）
     */
    public static String generateOrderNumber(Long userId) {
        // 1. 获取当前时间戳（精确到毫秒）
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(FORMATTER);

        // 2. 处理用户ID（不足6位左补0，超过6位取后6位）
        String userIdStr = String.format("%06d", userId % 1000000);

        // 3. 获取序列号（原子递增，超过最大值则重置）
        int sequence = COUNTER.getAndIncrement();
        if (sequence >= MAX_SEQUENCE) {
            COUNTER.set(0);
            sequence = 0;
        }
        String seqStr = String.format("%04d", sequence);

        // 4. 生成4位随机数
        String randomStr = String.format("%04d", (int) (Math.random() * 10000));

        // 5. 组合各部分
        return ORDER_PREFIX + timestamp + userIdStr + seqStr + randomStr;
    }
}
