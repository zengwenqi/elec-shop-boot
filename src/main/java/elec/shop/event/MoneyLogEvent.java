package elec.shop.event;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务日志事件
 * 用于异步记录财务操作日志
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoneyLogEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型 (1:超级管理员 2:管理员 3:采购员 4:商户)
     */
    private Integer userType;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    /**
     * 操作前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 操作后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 关联订单号
     */
    private String relatedOrderNo;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 状态 (1:成功 2:失败 3:处理中)
     */
    private Integer status;

    /**
     * 事件创建时间
     */
    private LocalDateTime eventTime;

    /**
     * 业务发生时间
     */
    private LocalDateTime businessTime;

    /**
     * 事件来源
     */
    private String source;

    /**
     * 事件ID（用于去重）
     */
    private String eventId;

    /**
     * 充值事件
     */
    public static MoneyLogEvent createRechargeEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress) {
        return createRechargeEvent(userId, username, userType, amount, balanceBefore, 
                                 balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 充值事件（支持自定义货币）
     */
    public static MoneyLogEvent createRechargeEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress, String currency) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType("RECHARGE")
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description("充值")
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId("RECHARGE", userId, relatedOrderNo))
                .build();
    }

    /**
     * 采购事件
     */
    public static MoneyLogEvent createPurchaseEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress) {
        return createPurchaseEvent(userId, username, userType, amount, balanceBefore, 
                                 balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 采购事件（支持自定义货币）
     */
    public static MoneyLogEvent createPurchaseEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress, String currency) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType("PURCHASE")
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description("采购支付")
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId("PURCHASE", userId, relatedOrderNo))
                .build();
    }

    /**
     * 退款事件
     */
    public static MoneyLogEvent createRefundEvent(Long userId, String username, Integer userType,
                                                 BigDecimal amount, BigDecimal balanceBefore,
                                                 BigDecimal balanceAfter, String relatedOrderNo,
                                                 String operatorName, String ipAddress) {
        return createRefundEvent(userId, username, userType, amount, balanceBefore, 
                               balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 退款事件（支持自定义货币）
     */
    public static MoneyLogEvent createRefundEvent(Long userId, String username, Integer userType,
                                                 BigDecimal amount, BigDecimal balanceBefore,
                                                 BigDecimal balanceAfter, String relatedOrderNo,
                                                 String operatorName, String ipAddress, String currency) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType("REFUND")
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description("商品退款")
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId("REFUND", userId, relatedOrderNo))
                .build();
    }

    /**
     * 佣金事件
     */
    public static MoneyLogEvent createCommissionEvent(Long userId, String username, Integer userType,
                                                     BigDecimal amount, BigDecimal balanceBefore,
                                                     BigDecimal balanceAfter, String relatedOrderNo,
                                                     String operatorName, String ipAddress) {
        return createCommissionEvent(userId, username, userType, amount, balanceBefore, 
                                   balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 佣金事件（支持自定义货币）
     */
    public static MoneyLogEvent createCommissionEvent(Long userId, String username, Integer userType,
                                                     BigDecimal amount, BigDecimal balanceBefore,
                                                     BigDecimal balanceAfter, String relatedOrderNo,
                                                     String operatorName, String ipAddress, String currency) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType("COMMISSION")
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description("采购佣金")
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId("COMMISSION", userId, relatedOrderNo))
                .build();
    }

    /**
     * 提现事件
     */
    public static MoneyLogEvent createWithdrawEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress) {
        return createWithdrawEvent(userId, username, userType, amount, balanceBefore, 
                                 balanceAfter, relatedOrderNo, operatorName, ipAddress, "CNY");
    }

    /**
     * 提现事件（支持自定义货币）
     */
    public static MoneyLogEvent createWithdrawEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress, String currency) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType("WITHDRAW")
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description("提现")
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId("WITHDRAW", userId, relatedOrderNo))
                .build();
    }

    /**
     * 转账事件
     */
    public static MoneyLogEvent createTransferEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress, boolean isOut) {
        return createTransferEvent(userId, username, userType, amount, balanceBefore, 
                                 balanceAfter, relatedOrderNo, operatorName, ipAddress, isOut, "CNY");
    }

    /**
     * 转账事件（支持自定义货币）
     */
    public static MoneyLogEvent createTransferEvent(Long userId, String username, Integer userType,
                                                   BigDecimal amount, BigDecimal balanceBefore,
                                                   BigDecimal balanceAfter, String relatedOrderNo,
                                                   String operatorName, String ipAddress, boolean isOut, String currency) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType("TRANSFER")
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description(isOut ? "转账支出" : "转账收入")
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId("TRANSFER", userId, relatedOrderNo))
                .build();
    }

    /**
     * 自定义事件（完全自定义操作类型和货币）
     */
    public static MoneyLogEvent createCustomEvent(Long userId, String username, Integer userType,
                                                 String operationType, BigDecimal amount, 
                                                 BigDecimal balanceBefore, BigDecimal balanceAfter,
                                                 String currency, String relatedOrderNo, String description,
                                                 String operatorName, String ipAddress) {
        return MoneyLogEvent.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .operationType(operationType)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .currency(currency)
                .relatedOrderNo(relatedOrderNo)
                .description(description)
                .operatorName(operatorName)
                .ipAddress(ipAddress)
                .status(1)
                .eventTime(LocalDateTime.now())
                .businessTime(LocalDateTime.now())
                .source("SYSTEM")
                .eventId(generateEventId(operationType, userId, relatedOrderNo))
                .build();
    }

    /**
     * 生成事件ID（用于去重）
     */
    private static String generateEventId(String type, Long userId, String orderNo) {
        return type + "_" + userId + "_" + orderNo + "_" + System.currentTimeMillis();
    }
}
