package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 交易流水表
 * @TableName finance_transaction
 */
@TableName(value ="finance_transaction")
@Data
public class FinanceTransaction implements Serializable {
    /**
     * 交易ID
     */
    @TableId
    private Long transactionId;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 关联账户ID
     */
    private String relatedAccountId;

    /**
     * 实际应付金额
     */
    private BigDecimal realPaymentMoney;

    /**
     * 交易类型：1-充值 2-提现 3-转账 4-支付 5-退款 6-结算
     */
    private Integer transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 状态：0-处理中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 支付凭证
     */
    private String paymentImage;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 软删除时间
     */
    private Date deletedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 乐观锁版本
     */
    private Integer version;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 逻辑删除：0-存在 1-删除，标识记录是否逻辑删除，默认0表示存在
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
