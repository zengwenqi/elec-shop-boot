package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("充值请求DTO")
public class RechargeRequestDTO {

    @ApiModelProperty(value = "币种", required = true, example = "CNY")
    private String currency;

    @ApiModelProperty(value = "充值金额", required = true, example = "100.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "实际应付金额", required = true, example = "100.00")
    private BigDecimal realPaymentMoney;

    @ApiModelProperty(value = "支付方式", required = true, example = "alipay")
    private String paymentMethod;

    @ApiModelProperty(value = "支付凭证图片列表", example = "[\"https://example.com/image1.jpg\"]")
    private List<String> paymentImages;

    @ApiModelProperty(value = "备注", example = "充值备注信息")
    private String remark;
}
