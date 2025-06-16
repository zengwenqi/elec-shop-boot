package elec.shop.pojo.purchase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购订单商品明细数据传输对象
 */
@Data
@ApiModel("采购订单商品明细数据传输对象")
public class PurchaserOrderItemDTO {

    @ApiModelProperty(value = "商品明细ID", hidden = true)
    private Long itemId;

    @ApiModelProperty(value = "关联的采购订单ID", hidden = true)
    private Long orderId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品链接")
    private String productLink;

    @ApiModelProperty("商品图片路径")
    private String productImage;

    @ApiModelProperty("商品SKU")
    private String sku;

    @ApiModelProperty("商品单价")
    private BigDecimal unitPrice;

    @ApiModelProperty("采购数量")
    private Integer purchaseQuantity;
}
