package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 佣金查询参数
 */
@Data
@ApiModel("佣金查询参数")
public class CommissionQueryDTO {
    
    @ApiModelProperty("关键字（用户名、真实姓名、订单号）")
    private String keyword;
    
    @ApiModelProperty("用户类型：1-采购员 2-管理员")
    private Integer userType;
    
    @ApiModelProperty("佣金状态：0-待发放 1-已发放 2-已取消")
    private Integer commissionStatus;
    
    @ApiModelProperty("开始时间")
    private String startTime;
    
    @ApiModelProperty("结束时间")
    private String endTime;
    
    @ApiModelProperty("结算周期")
    private String settlementPeriod;
    
    @ApiModelProperty("页码")
    private Integer page = 1;
    
    @ApiModelProperty("每页大小")
    private Integer size = 10;
}