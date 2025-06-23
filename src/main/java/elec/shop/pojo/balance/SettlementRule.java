package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 结算规则表
 * @TableName settlement_rule
 */
@TableName(value ="settlement_rule")
@Data
public class SettlementRule implements Serializable {
    /**
     * 规则ID
     */
    @TableId
    private Long ruleId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 结算周期：1-T+1 2-T+7 3-T+15 4-月结
     */
    private Integer settlementCycle;

    /**
     * 最小结算金额
     */
    private BigDecimal minAmount;

    /**
     * 手续费率
     */
    private BigDecimal feeRate;

    /**
     * 最小手续费
     */
    private BigDecimal minFee;

    /**
     * 最大手续费
     */
    private BigDecimal maxFee;

    /**
     * 状态：1-启用 2-禁用
     */
    private Integer status;

    /**
     * 生效时间
     */
    private Date effectTime;

    /**
     * 失效时间
     */
    private Date expireTime;

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