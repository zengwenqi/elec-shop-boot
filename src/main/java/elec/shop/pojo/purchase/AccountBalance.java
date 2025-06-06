package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 账户余额表（多币种）
 * @TableName account_balance
 */
@TableName(value ="account_balance")
@Data
public class AccountBalance implements Serializable {
    /**
     * 余额记录ID
     */
    @TableId
    private Long balanceId;

    /**
     * 账户ID
     */
    private String accountId;

    /**
     * 币种代码
     */
    private String currency;

    /**
     * 币种符号
     */
    private String symbol;

    /**
     * 币种名称
     */
    private String currencyName;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 相对于基础币种的汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 最后更新时间
     */
    private Date lastUpdated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
