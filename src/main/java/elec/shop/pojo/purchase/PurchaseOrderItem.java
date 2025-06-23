package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 采购订单商品明细表
 * @TableName purchase_order_item
 */
@TableName(value ="purchase_order_item")
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderItem extends BaseEntity implements Serializable {
    /**
     * 商品明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long itemId;

    /**
     * 关联的采购订单ID
     */
    private Long orderId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品链接
     */
    private String productLink;

    /**
     * 商品图片路径
     */
    private String productImage;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 商品单价
     */
    private BigDecimal unitPrice;

    /**
     * 采购数量
     */
    private Integer purchaseQuantity;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
