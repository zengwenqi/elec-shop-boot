package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购订单数据传输对象 - 用于创建和更新订单
 */
@Data
@ApiModel("采购订单数据传输对象")
public class PurchaserOrderDTO {

    @ApiModelProperty(value = "订单ID", hidden = true)
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("店铺ID")
    private Long shopId;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("收货地址")
    private String infoAddress;

    @ApiModelProperty("收货人")
    private String reciverPerson;

    @ApiModelProperty("采购员ID")
    private Long purchaserId;

    @ApiModelProperty("订单类型")
    private Integer orderType;

    @ApiModelProperty("订单状态：0-待确认 1-已确认 2-采购中 3-已完成 4-已取消")
    private Integer orderStatus;

    @ApiModelProperty("支付状态：0-待支付 1-已支付 2-已退款")
    private Integer paymentStatus;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("币种")
    private String currency;

    @ApiModelProperty("汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty("回填单号")
    private String remarkOrderNo;

    @ApiModelProperty("订单备注")
    private String remark;

    @ApiModelProperty("取消原因")
    private String cancelReason;

    @ApiModelProperty("订单商品明细列表")
    private List<PurchaserOrderItemDTO> orderItems;
}
