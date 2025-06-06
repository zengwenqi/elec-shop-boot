package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("采购员查询参数")
public class PurchaserQueryDTO {
    @ApiModelProperty("采购员编号")
    private String purchaserCode;

    @ApiModelProperty("页码")
    private Integer page = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;
} 