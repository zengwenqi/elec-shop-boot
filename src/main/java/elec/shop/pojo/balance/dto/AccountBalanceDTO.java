package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("账户充值DTO")
public class AccountBalanceDTO {

    @ApiModelProperty("充值金额")
    private String money;

    @ApiModelProperty("用户ID")
    private Long userId;
}
