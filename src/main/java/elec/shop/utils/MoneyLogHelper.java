package elec.shop.utils;

import elec.shop.event.MoneyLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 财务日志工具类
 * 提供简单的静态方法调用接口，实现低耦合的日志记录
 */
@Slf4j
@Component
public class MoneyLogHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        MoneyLogHelper.applicationContext = context;
    }

    /**
     * 记录全自定义日志（完整版本，带操作员信息）
     * 自动根据变动前后金额判断是扣款还是增款：
     * - 变动前金额 > 变动后金额：扣款（负数）
     * - 变动前金额 < 变动后金额：增款（正数）
     * - 变动前金额 = 变动后金额：默认增款0
     */
    public static void zidingyiLogRecharge(Long userId, String username, Integer userType,
                                          String operationType, BigDecimal amount,
                                          BigDecimal balanceBefore, BigDecimal balanceAfter,
                                          String relatedOrderNo, String description,
                                          String operatorName,
                                          String source, String currency) {
        try {
            // 根据变动前后金额自动计算实际金额变动
            BigDecimal actualAmount;
            if (balanceBefore.compareTo(balanceAfter) > 0) {
                // 变动前 > 变动后：扣款，使用负数
                actualAmount = balanceAfter.subtract(balanceBefore);
            } else if (balanceBefore.compareTo(balanceAfter) < 0) {
                // 变动前 < 变动后：增款，使用正数
                actualAmount = balanceAfter.subtract(balanceBefore);
            } else {
                // 变动前 = 变动后：默认增款0
                actualAmount = BigDecimal.ZERO;
            }

            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.zidingyiEvent(
                    userId, username, userType, operationType, actualAmount,
                    balanceBefore, balanceAfter, relatedOrderNo, description,
                    operatorName, ipAddress, source, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录自定义日志失败: userId={}, operationType={}, amount={}",
                     userId, operationType, amount, e);
        }
    }

    /**
     * 记录充值日志
     */
    public static void logRecharge(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo) {
        logRecharge(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, "CNY");
    }

    /**
     * 记录充值日志（支持自定义货币）
     */
    public static void logRecharge(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, String currency) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createRechargeEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录充值日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录充值日志（带操作员信息）
     */
    public static void logRecharge(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo,
                                  String operatorName, String ipAddress) {
        logRecharge(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 记录充值日志（带操作员信息和自定义货币）
     */
    public static void logRecharge(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo,
                                  String operatorName, String ipAddress, String currency) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createRechargeEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录充值日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录采购日志
     */
    public static void logPurchase(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo) {
        logPurchase(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, "CNY");
    }

    /**
     * 记录采购日志（支持自定义货币）
     */
    public static void logPurchase(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, String currency) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createPurchaseEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录采购日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录采购日志（带操作员信息）
     */
    public static void logPurchase(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo,
                                  String operatorName, String ipAddress) {
        logPurchase(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 记录采购日志（带操作员信息和自定义货币）
     */
    public static void logPurchase(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo,
                                  String operatorName, String ipAddress, String currency) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createPurchaseEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录采购日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录退款日志
     */
    public static void logRefund(Long userId, String username, Integer userType,
                                BigDecimal amount, BigDecimal balanceBefore,
                                BigDecimal balanceAfter, String relatedOrderNo) {
        logRefund(userId, username, userType, amount, balanceBefore,
                 balanceAfter, relatedOrderNo, "CNY");
    }

    /**
     * 记录退款日志（支持自定义货币）
     */
    public static void logRefund(Long userId, String username, Integer userType,
                                BigDecimal amount, BigDecimal balanceBefore,
                                BigDecimal balanceAfter, String relatedOrderNo, String currency) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createRefundEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录退款日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录退款日志（带操作员信息）
     */
    public static void logRefund(Long userId, String username, Integer userType,
                                BigDecimal amount, BigDecimal balanceBefore,
                                BigDecimal balanceAfter, String relatedOrderNo,
                                String operatorName, String ipAddress) {
        logRefund(userId, username, userType, amount, balanceBefore,
                 balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 记录退款日志（带操作员信息和自定义货币）
     */
    public static void logRefund(Long userId, String username, Integer userType,
                                BigDecimal amount, BigDecimal balanceBefore,
                                BigDecimal balanceAfter, String relatedOrderNo,
                                String operatorName, String ipAddress, String currency) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createRefundEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录退款日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录佣金日志
     */
    public static void logCommission(Long userId, String username, Integer userType,
                                    BigDecimal amount, BigDecimal balanceBefore,
                                    BigDecimal balanceAfter, String relatedOrderNo) {
        logCommission(userId, username, userType, amount, balanceBefore,
                     balanceAfter, relatedOrderNo, "CNY");
    }

    /**
     * 记录佣金日志（支持自定义货币）
     */
    public static void logCommission(Long userId, String username, Integer userType,
                                    BigDecimal amount, BigDecimal balanceBefore,
                                    BigDecimal balanceAfter, String relatedOrderNo, String currency) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createCommissionEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录佣金日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录佣金日志（带操作员信息）
     */
    public static void logCommission(Long userId, String username, Integer userType,
                                    BigDecimal amount, BigDecimal balanceBefore,
                                    BigDecimal balanceAfter, String relatedOrderNo,
                                    String operatorName, String ipAddress) {
        logCommission(userId, username, userType, amount, balanceBefore,
                     balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 记录佣金日志（带操作员信息和自定义货币）
     */
    public static void logCommission(Long userId, String username, Integer userType,
                                    BigDecimal amount, BigDecimal balanceBefore,
                                    BigDecimal balanceAfter, String relatedOrderNo,
                                    String operatorName, String ipAddress, String currency) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createCommissionEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录佣金日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录提现日志
     */
    public static void logWithdraw(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo) {
        logWithdraw(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, "CNY");
    }

    /**
     * 记录提现日志（支持自定义货币）
     */
    public static void logWithdraw(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, String currency) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createWithdrawEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录提现日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录提现日志（带操作员信息）
     */
    public static void logWithdraw(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo,
                                  String operatorName, String ipAddress) {
        logWithdraw(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 记录提现日志（带操作员信息和自定义货币）
     */
    public static void logWithdraw(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo,
                                  String operatorName, String ipAddress, String currency) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createWithdrawEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录提现日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录转账日志
     */
    public static void logTransfer(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, boolean isOut) {
        logTransfer(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, isOut, "CNY");
    }

    /**
     * 记录转账日志（支持自定义货币）
     */
    public static void logTransfer(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, boolean isOut, String currency) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createTransferEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, isOut, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录转账日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录转账日志（带操作员信息）
     */
    public static void logTransfer(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, boolean isOut,
                                  String operatorName, String ipAddress) {
        logTransfer(userId, username, userType, amount, balanceBefore,
                   balanceAfter, relatedOrderNo, isOut, operatorName, ipAddress, "CNY");
    }

    /**
     * 记录转账日志（带操作员信息和自定义货币）
     */
    public static void logTransfer(Long userId, String username, Integer userType,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, String relatedOrderNo, boolean isOut,
                                  String operatorName, String ipAddress, String currency) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createTransferEvent(
                    userId, username, userType, amount, balanceBefore,
                    balanceAfter, relatedOrderNo, operatorName, ipAddress, isOut, currency);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录转账日志失败: userId={}, amount={}", userId, amount, e);
        }
    }

    /**
     * 记录自定义日志（完全自定义操作类型和货币）
     */
    public static void logCustom(Long userId, String username, Integer userType,
                                String operationType, BigDecimal amount,
                                BigDecimal balanceBefore, BigDecimal balanceAfter,
                                String currency, String relatedOrderNo, String description) {
        try {
            String operatorName = getCurrentOperatorName();
            String ipAddress = getCurrentIpAddress();

            MoneyLogEvent event = MoneyLogEvent.createCustomEvent(
                    userId, username, userType, operationType, amount,
                    balanceBefore, balanceAfter, currency, relatedOrderNo,
                    description, operatorName, ipAddress);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录自定义日志失败: userId={}, operationType={}, amount={}",
                     userId, operationType, amount, e);
        }
    }

    /**
     * 记录自定义日志（带操作员信息）
     */
    public static void logCustom(Long userId, String username, Integer userType,
                                String operationType, BigDecimal amount,
                                BigDecimal balanceBefore, BigDecimal balanceAfter,
                                String currency, String relatedOrderNo, String description,
                                String operatorName, String ipAddress) {
        try {
            MoneyLogEvent event = MoneyLogEvent.createCustomEvent(
                    userId, username, userType, operationType, amount,
                    balanceBefore, balanceAfter, currency, relatedOrderNo,
                    description, operatorName, ipAddress);

            publishEvent(event);

        } catch (Exception e) {
            log.error("记录自定义日志失败: userId={}, operationType={}, amount={}",
                     userId, operationType, amount, e);
        }
    }

    /**
     * 记录自定义事件（直接传入事件对象）
     */
    public static void logCustom(MoneyLogEvent event) {
        try {
            publishEvent(event);
            log.debug("发布财务日志事件: {}", event.getEventId());
        } catch (Exception e) {
            log.error("记录自定义事件失败: eventId={}", event.getEventId(), e);
        }
    }

    /**
     * 发布事件
     */
    private static void publishEvent(MoneyLogEvent event) {
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        } else {
            log.warn("ApplicationContext未初始化，无法发布财务日志事件: {}", event.getEventId());
        }
    }

    /**
     * 获取当前操作员名称
     */
    private static String getCurrentOperatorName() {
        try {
            // 这里可以从SecurityContext或其他地方获取当前用户信息
            // 暂时返回默认值
            return "系统操作";
        } catch (Exception e) {
            return "未知操作员";
        }
    }

    /**
     * 获取当前IP地址
     */
    private static String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return getClientIpAddress(request);
            }

            return "127.0.0.1";

        } catch (Exception e) {
            return "未知IP";
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
