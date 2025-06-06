package elec.shop.pojo.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PurchaseOrderDTO {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 订单状态：0-待确认 1-已确认 2-采购中 3-已完成 4-已取消
     */
    private Integer orderStatus;

    /**
     * 支付状态：0-待支付 1-已支付 2-已退款
     */
    private Integer paymentStatus;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 回填单号
     */
    private String remarkOrderNo;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 确认时间
     */
    private Date confirmTime;

    /**
     * 完成时间
     */
    private Date completeTime;

}
