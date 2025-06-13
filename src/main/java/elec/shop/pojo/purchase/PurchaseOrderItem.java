package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 采购订单商品明细表
 * @TableName purchase_order_item
 */
@TableName(value ="purchase_order_item")
@Data
public class PurchaseOrderItem implements Serializable {
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

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 软删除时间
     */
    private Date deletedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 乐观锁版本
     */
    private Integer version;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 逻辑删除：0-存在 1-删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
