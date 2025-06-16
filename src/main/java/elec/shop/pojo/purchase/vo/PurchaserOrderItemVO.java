package elec.shop.pojo.purchase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购订单商品明细视图对象
 */
@Data
@ApiModel("采购订单商品明细视图对象")
public class PurchaserOrderItemVO {

    @ApiModelProperty("商品明细ID")
    private Long itemId;

    @ApiModelProperty("关联的采购订单ID")
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

    @ApiModelProperty("商品小计金额")
    private BigDecimal subtotalAmount;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}
