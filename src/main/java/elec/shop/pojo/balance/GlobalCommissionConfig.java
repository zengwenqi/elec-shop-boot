package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import elec.shop.pojo.base.BaseEntity;
import elec.shop.utils.RangeSearchUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 全局佣金配置表
 * @TableName global_commission_config
 */
@TableName(value = "global_commission_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class GlobalCommissionConfig extends BaseEntity implements Serializable, RangeSearchUtil.Rangeable {

    /**
     * 配置ID
     */
    @TableId
    private Long configId;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 用户类型：1-采购员 2-管理员
     */
    private Integer userType;

    /**
     * 佣金类型：1-固定金额 2-百分比
     */
    private Integer commissionType;

    /**
     * 佣金值（固定金额或百分比）
     */
    private BigDecimal commissionValue;

    /**
     * 最小佣金金额
     */
    private BigDecimal minCommission;

    /**
     * 最大佣金金额
     */
    private BigDecimal maxCommission;

    /**
     * 生效时间
     */
    private Date effectiveTime;

    /**
     * 失效时间
     */
    private Date expiryTime;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public double getMin() {
        return Double.parseDouble(this.getMinCommission().toString());
    }

    @Override
    public double getMax() {
        return Double.parseDouble(this.getMaxCommission().toString());
    }
}
