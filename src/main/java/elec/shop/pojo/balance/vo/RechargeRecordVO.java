package elec.shop.pojo.balance.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("充值记录VO")
public class RechargeRecordVO {

    @ApiModelProperty("交易ID")
    private String transactionId;

    @ApiModelProperty("交易编号")
    private String transactionNo;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("账户ID")
    private String accountId;

    @ApiModelProperty("用户ID（管理员视图）")
    private String userId;

    @ApiModelProperty("用户名（管理员视图）")
    private String username;

    @ApiModelProperty("充值金额")
    private BigDecimal amount;

    @ApiModelProperty("币种")
    private String currency;

    @ApiModelProperty("支付方式")
    private String paymentMethod;

    @ApiModelProperty("支付方式（显示用）")
    private String payMethod;

    @ApiModelProperty("应支付金额")
    private BigDecimal realPaymentMoney;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("支付凭证图片")
    private List<String> paymentImages;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

    @ApiModelProperty("完成时间")
    private String completeTime;
}
