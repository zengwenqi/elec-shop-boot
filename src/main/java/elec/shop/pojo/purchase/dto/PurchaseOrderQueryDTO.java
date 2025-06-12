package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("采购订单查询参数")
public class PurchaseOrderQueryDTO {
    @ApiModelProperty("商店ID")
    private Integer shopId;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("支付状态")
    private Integer paymentStatus;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("页码")
    private Integer page = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;
}
