package elec.shop.pojo.balance.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户佣金配置视图对象
 */
@Data
@ApiModel("用户佣金配置视图对象")
public class UserCommissionConfigVO {
    
    @ApiModelProperty("配置ID")
    private Long configId;
    
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
    
    @ApiModelProperty("佣金类型：1-固定金额 2-百分比")
    private Integer commissionType;
    
    @ApiModelProperty("佣金类型名称")
    private String commissionTypeName;
    
    @ApiModelProperty("佣金值（固定金额或百分比）")
    private BigDecimal commissionValue;
    
    @ApiModelProperty("最小佣金金额")
    private BigDecimal minCommission;
    
    @ApiModelProperty("最大佣金金额")
    private BigDecimal maxCommission;
    
    @ApiModelProperty("生效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveTime;
    
    @ApiModelProperty("失效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiryTime;
    
    @ApiModelProperty("状态：0-禁用 1-启用")
    private Integer status;
    
    @ApiModelProperty("状态名称")
    private String statusName;
    
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