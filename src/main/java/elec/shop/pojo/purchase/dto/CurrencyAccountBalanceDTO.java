package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("汇率数据传输对象")
public class CurrencyAccountBalanceDTO {
    @ApiModelProperty("货币代码")
    private String currency;
    @ApiModelProperty("账户编号")
    private String accountNo;
    @ApiModelProperty("余额")
    private BigDecimal balance;

    // 1：增加余额  2：减少余额
    @ApiModelProperty("操作状态")
    private Integer operationStatus;
}
