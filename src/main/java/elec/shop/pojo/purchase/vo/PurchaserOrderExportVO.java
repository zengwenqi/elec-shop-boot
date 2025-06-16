package elec.shop.pojo.purchase.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER, wrapped = BooleanEnum.TRUE)
public class PurchaserOrderExportVO {

    // 基本信息字段（相同数据会自动合并）
    @ColumnWidth(30)
    @ExcelProperty(value = "订单编号", index = 0)
    private String orderNo;

    @ColumnWidth(15)
    @ExcelProperty(value = "店铺名称", index = 1)
    private String shopName;

    @ColumnWidth(15)
    @ExcelProperty(value = "店铺编号", index = 2)
    private String shopCode;

    @ColumnWidth(15)
    @ExcelProperty(value = "采购员编号", index = 3)
    private String purchaserCode;

    @ColumnWidth(15)
    @ExcelProperty(value = "订单类型", index = 4)
    private String orderType;

    @ColumnWidth(15)
    @ExcelProperty(value = "联系电话", index = 5)
    private String contactPhone;

    @ColumnWidth(30)
    @ExcelProperty(value = "收货地址", index = 6)
    private String infoAddress;

    @ColumnWidth(15)
    @ExcelProperty(value = "收货人", index = 7)
    private String reciverPerson;

    @ColumnWidth(15)
    @ExcelProperty(value = "订单状态", index = 8)
    private String orderStatus;

    @ColumnWidth(15)
    @ExcelProperty(value = "支付状态", index = 9)
    private String paymentStatus;

    @ColumnWidth(15)
    @NumberFormat("#.##")
    @ExcelProperty(value = "订单金额", index = 10)
    private BigDecimal totalAmount;

    @ColumnWidth(10)
    @ExcelProperty(value = "币种", index = 11)
    private String currency;

    @ColumnWidth(10)
    @NumberFormat("#.###")
    @ExcelProperty(value = "汇率", index = 12)
    private BigDecimal exchangeRate;

    @ColumnWidth(30)
    @ExcelProperty(value = "回填单号", index = 13)
    private String remarkOrderNo;

    @ColumnWidth(20)
    @ExcelProperty(value = "订单备注", index = 14)
    private String remark;

    @ColumnWidth(20)
    @ExcelProperty(value = "取消原因", index = 15)
    private String cancelReason;

    @ColumnWidth(20)
    @ExcelProperty(value = "取消时间", index = 16)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "确认时间", index = 17)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "完成时间", index = 18)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 19)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @ColumnWidth(15)
    @ExcelProperty(value = "创建人", index = 20)
    private String createdBy;

    @ColumnWidth(20)
    @ExcelProperty(value = "更新时间", index = 21)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    @ColumnWidth(15)
    @ExcelProperty(value = "更新人", index = 22)
    private String updatedBy;

    // 商品明细字段（不会合并）
    @ColumnWidth(30)
    @ExcelProperty(value = "商品名称", index = 23)
    private String productName;

    @ColumnWidth(50)
    @ExcelProperty(value = "商品链接", index = 24)
    private String productLink;

    @ColumnWidth(50)
    @ExcelProperty(value = "商品图片", index = 25)
    private String productImage;

    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 26)
    private String sku;

    @ColumnWidth(15)
    @NumberFormat("#.##")
    @ExcelProperty(value = "单价", index = 27)
    private BigDecimal unitPrice;

    @ColumnWidth(10)
    @ExcelProperty(value = "采购数量", index = 28)
    private Integer purchaseQuantity;
}
