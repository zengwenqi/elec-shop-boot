package elec.shop.pojo.finance;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("money_log")
public class MoneyLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @ExcelIgnore
    private Long logId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @ExcelIgnore
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    @ExcelProperty("用户名")
    private String username;

    /**
     * 用户类型：1-超级管理员，2-管理员，3-采购员，4-商户
     */
    @TableField("user_type")
    @ExcelProperty("用户类型")
    private Integer userType;

    /**
     * 操作类型：RECHARGE-充值，PURCHASE-采购支出，REFUND-退款，COMMISSION-佣金，WITHDRAW-提现，TRANSFER-转账
     */
    @TableField("operation_type")
    @ExcelProperty("操作类型")
    private String operationType;

    /**
     * 变动金额（正数为收入，负数为支出）
     */
    @TableField("amount")
    @ExcelProperty("变动金额")
    private BigDecimal amount;

    /**
     * 操作前余额
     */
    @TableField("balance_before")
    @ExcelProperty("操作前余额")
    private BigDecimal balanceBefore;

    /**
     * 操作后余额
     */
    @TableField("balance_after")
    @ExcelProperty("操作后余额")
    private BigDecimal balanceAfter;

    /**
     * 货币类型
     */
    @TableField("currency")
    @ExcelProperty("货币类型")
    private String currency;

    /**
     * 关联订单号
     */
    @TableField("related_order_no")
    @ExcelProperty("关联订单号")
    private String relatedOrderNo;

    /**
     * 关联业务ID
     */
    @TableField("related_id")
    @ExcelIgnore
    private Long relatedId;

    /**
     * 操作描述
     */
    @TableField("description")
    @ExcelProperty("操作描述")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    @ExcelProperty("备注")
    private String remark;

    /**
     * 操作员ID（如果是管理员操作）
     */
    @TableField("operator_id")
    @ExcelIgnore
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @TableField("operator_name")
    @ExcelProperty("操作员姓名")
    private String operatorName;

    /**
     * IP地址
     */
    @TableField("ip_address")
    @ExcelProperty("IP地址")
    private String ipAddress;

    /**
     * 状态：1-成功，2-失败，3-处理中
     */
    @TableField("status")
    @ExcelProperty("状态")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ExcelProperty("创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ExcelProperty("更新时间")
    private LocalDateTime updatedAt;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        RECHARGE("RECHARGE", "充值"),
        PURCHASE("PURCHASE", "采购支出"),
        REFUND("REFUND", "退款"),
        COMMISSION("COMMISSION", "佣金"),
        WITHDRAW("WITHDRAW", "提现"),
        TRANSFER("TRANSFER", "转账");

        private final String code;
        private final String desc;

        OperationType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        SUCCESS(1, "成功"),
        FAILED(2, "失败"),
        PROCESSING(3, "处理中");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static String fromCode(Integer code) {
            for (Status value : Status.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
}
