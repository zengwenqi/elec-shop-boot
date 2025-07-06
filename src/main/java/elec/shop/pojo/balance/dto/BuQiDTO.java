package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("补齐差价DTO")
public class BuQiDTO {

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("补齐金额")
    private BigDecimal amount;

    @ApiModelProperty("币种")
    private String currency;
}
