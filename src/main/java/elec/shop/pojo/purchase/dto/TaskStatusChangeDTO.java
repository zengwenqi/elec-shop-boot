package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("采购任务状态变更请求")
public class TaskStatusChangeDTO {
    @ApiModelProperty(value = "任务ID/订单ID", required = true)
    private Long taskId;

    @ApiModelProperty(value = "新的任务状态", required = true)
    private Integer newStatus;

    @ApiModelProperty("状态变更备注（可选）")
    private String remark;
}
