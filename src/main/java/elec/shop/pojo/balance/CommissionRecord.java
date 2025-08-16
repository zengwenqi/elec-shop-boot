package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import elec.shop.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 佣金记录表
 * @TableName commission_record
 */
@TableName(value = "commission_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionRecord extends BaseEntity implements Serializable {
    
    /**
     * 记录ID
     */
    @TableId
    private Long recordId;
    
    /**
     * 佣金编号
     */
    private String commissionNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 用户类型：1-采购员 2-管理员
     */
    private Integer userType;
    
    /**
     * 关联订单ID
     */
    private Long orderId;
    
    /**
     * 关联订单号
     */
    private String orderNo;
    
    /**
     * 订单金额
     */
    private BigDecimal orderAmount;
    
    /**
     * 佣金类型：1-固定金额 2-百分比
     */
    private Integer commissionType;
    
    /**
     * 佣金率/固定金额
     */
    private BigDecimal commissionRate;
    
    /**
     * 佣金金额
     */
    private BigDecimal commissionAmount;
    
    /**
     * 佣金状态：0-待发放 1-已发放 2-已取消
     */
    private Integer commissionStatus;
    
    /**
     * 发放时间
     */
    private Date payoutTime;
    
    /**
     * 结算周期
     */
    private String settlementPeriod;
    
    /**
     * 备注
     */
    private String remark;
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}