package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("采购任务查询参数")
public class PurchaserTaskQueryDTO {
    @ApiModelProperty("任务状态")
    private Integer taskStatus;

    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("任务ID")
    private Long taskId;

    @ApiModelProperty("页码")
    private Integer page = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;
}
