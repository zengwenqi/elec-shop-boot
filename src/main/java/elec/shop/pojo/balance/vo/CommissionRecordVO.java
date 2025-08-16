package elec.shop.pojo.balance.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 佣金记录视图对象
 */
@Data
@ApiModel("佣金记录视图对象")
public class CommissionRecordVO {
    
    @ApiModelProperty("记录ID")
    private Long recordId;
    
    @ApiModelProperty("佣金编号")
    private String commissionNo;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("真实姓名")
    private String realName;
    
    @ApiModelProperty("用户类型：1-采购员 2-管理员")
    private Integer userType;
    
    @ApiModelProperty("用户类型名称")
    private String userTypeName;
    
    @ApiModelProperty("关联订单ID")
    private Long orderId;
    
    @ApiModelProperty("关联订单号")
    private String orderNo;
    
    @ApiModelProperty("订单金额")
    private BigDecimal orderAmount;
    
    @ApiModelProperty("佣金类型：1-固定金额 2-百分比")
    private Integer commissionType;
    
    @ApiModelProperty("佣金类型名称")
    private String commissionTypeName;
    
    @ApiModelProperty("佣金率/固定金额")
    private BigDecimal commissionRate;
    
    @ApiModelProperty("佣金金额")
    private BigDecimal commissionAmount;
    
    @ApiModelProperty("佣金状态：0-待发放 1-已发放 2-已取消")
    private Integer commissionStatus;
    
    @ApiModelProperty("佣金状态名称")
    private String commissionStatusName;
    
    @ApiModelProperty("发放时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payoutTime;
    
    @ApiModelProperty("结算周期")
    private String settlementPeriod;
    
    @ApiModelProperty("备注")
    private String remark;
    
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
    
    @ApiModelProperty("创建人")
    private String createdBy;
    
    @ApiModelProperty("更新人")
    private String updatedBy;
}