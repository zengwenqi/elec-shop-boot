package elec.shop.pojo.purchase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 采购订单视图对象
 */
@Data
@ApiModel("采购订单视图对象")
public class PurchaserOrderVO {

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("跨境采购平台")
    private String crossService;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("店铺ID")
    private Long shopId;

    @ApiModelProperty("店铺名称")
    private String shopName;

    @ApiModelProperty("采购员ID")
    private Long purchaserId;

    @ApiModelProperty("采购员姓名")
    private String purchaserName;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("收货地址")
    private String infoAddress;

    @ApiModelProperty("收货地址二")
    private String infoAddressBei;

    @ApiModelProperty("收货人")
    private String reciverPerson;

    @ApiModelProperty("订单类型")
    private Integer orderType;

    @ApiModelProperty("订单类型名称")
    private String orderTypeName;

    @ApiModelProperty("订单状态：0-待分配 1-待确认 2-已确认 3-采购中 4-已完成 5-已取消")
    private Integer orderStatus;

    @ApiModelProperty("订单状态名称")
    private String orderStatusName;

    @ApiModelProperty("支付状态：0-待支付 1-已支付 2-已退款")
    private Integer paymentStatus;

    @ApiModelProperty("支付状态名称")
    private String paymentStatusName;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("手续费")
    private BigDecimal serviceCharge;

    @ApiModelProperty("实际订单总金额")
    private BigDecimal realTotalAmount;

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

    @ApiModelProperty("取消时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

    @ApiModelProperty("确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    @ApiModelProperty("创建人ID")
    private Long createdBy;

    @ApiModelProperty("创建人姓名")
    private String createdByName;

    @ApiModelProperty("更新人ID")
    private Long updatedBy;

    @ApiModelProperty("更新人姓名")
    private String updatedByName;

    @ApiModelProperty("订单商品明细列表")
    private List<PurchaserOrderItemVO> orderItems;
}
