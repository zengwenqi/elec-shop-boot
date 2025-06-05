package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 账户信息表
 * @TableName finance_account
 */
@TableName(value ="finance_account")
@Data
public class FinanceAccount implements Serializable {
    /**
     * 账户ID
     */
    @TableId
    private Long accountId;

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
     * 状态：1-正常 2-冻结 3-注销
     */
    private Integer status;

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
     * 逻辑删除：0-存在 1-删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
