package elec.shop.pojo.purchase.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("汇率信息")
public class ExchangeRateVO {

    @ApiModelProperty("币种代码")
    private String currency;

    @ApiModelProperty("币种符号")
    private String symbol;

    @ApiModelProperty("币种名称")
    private String currencyName;

    @ApiModelProperty("相对于基础币种的汇率")
    private BigDecimal exchangeRate;
}
