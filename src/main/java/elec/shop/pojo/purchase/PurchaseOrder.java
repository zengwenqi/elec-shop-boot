package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购订单主表
 * @TableName purchase_order
 */
@TableName(value ="purchase_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity implements Serializable {
    /**
     * 订单ID
     */
    @TableId
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 跨境采购平台
     */
    private String crossService;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 采购员ID
     */
    private Long purchaserId;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 订单状态：0-待分配 1-待确认 2-已确认 3-采购中 4-已下单 5-已出面单 6-已完成 7-已取消
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
     * 手续费
     */
    private BigDecimal serviceCharge;

    /**
     * 实际订单总金额
     */
    private BigDecimal realTotalAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 州
     */
    private String state;

    /**
     * 城市
     */
    private String city;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 收货地址
     */
    private String infoAddress;

    /**
     * 详细地址二
     */
    private String infoAddressBei;

    /**
     * 收货人
     */
    private String reciverPerson;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 商品顶单号
     */
    private String goodsOrderNo;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
