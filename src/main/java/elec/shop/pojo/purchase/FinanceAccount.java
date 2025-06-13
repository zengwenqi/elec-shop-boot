package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;

/**
 * 账户信息表
 * @TableName finance_account
 */
@TableName(value ="finance_account")
@Data
public class FinanceAccount extends BaseEntity implements Serializable {
    /**
     * 账户ID
     */
    @TableId
    private String accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户类型：1-商户 2-平台
     */
    private Integer accountType;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 基础币种
     */
    private String baseCurrency;

    /**
     * 账户余额
     */
    private BigDecimal banlance;

    /**
     * 状态：1-正常 2-冻结 3-注销
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
