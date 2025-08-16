package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户佣金配置数据传输对象
 */
@Data
@ApiModel("用户佣金配置数据传输对象")
public class UserCommissionConfigDTO {

    @ApiModelProperty(value = "配置ID", hidden = true)
    private Long configId;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty(value = "用户类型：1-采购员 2-管理员", required = true)
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @ApiModelProperty(value = "佣金类型：1-固定金额 2-百分比", required = true)
    @NotNull(message = "佣金类型不能为空")
    private Integer commissionType;

    @ApiModelProperty(value = "佣金值（固定金额或百分比）", required = true)
    @NotNull(message = "佣金值不能为空")
    @DecimalMin(value = "0", message = "佣金值不能小于0")
    private BigDecimal commissionValue;

    @ApiModelProperty("最小佣金金额")
    @DecimalMin(value = "0", message = "最小佣金金额不能小于0")
    private BigDecimal minCommission;

    @ApiModelProperty("最大佣金金额")
    @DecimalMin(value = "0", message = "最大佣金金额不能小于0")
    private BigDecimal maxCommission;

    @ApiModelProperty("生效时间")
    private Date effectiveTime;

    @ApiModelProperty("失效时间")
    private Date expiryTime;

    @ApiModelProperty("状态：0-禁用 1-启用")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;
}
