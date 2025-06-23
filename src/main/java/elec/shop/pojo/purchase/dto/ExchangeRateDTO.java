package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("汇率数据传输对象")
public class ExchangeRateDTO {
    @ApiModelProperty("货币代码")
    private String currency;
    @ApiModelProperty("汇率")
    private BigDecimal exchangeRate;
    @ApiModelProperty("账户ID")
    private String accountId;
}
