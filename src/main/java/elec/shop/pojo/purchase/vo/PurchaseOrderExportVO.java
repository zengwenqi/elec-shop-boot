package elec.shop.pojo.purchase.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PurchaseOrderExportVO {
    
    @ExcelProperty("订单编号")
    private String orderNo;
    
    @ExcelProperty("店铺名称")
    private String shopName;
    
    @ExcelProperty("店铺编号")
    private String shopCode;
    
    @ExcelProperty("采购员编号")
    private String purchaserCode;
    
    @ExcelProperty("订单类型")
    private String orderType;
    
    @ExcelProperty("订单状态")
    private String orderStatus;
    
    @ExcelProperty("支付状态")
    private String paymentStatus;
    
    @ExcelProperty("订单金额")
    private BigDecimal totalAmount;
    
    @ExcelProperty("币种")
    private String currency;
    
    @ExcelProperty("汇率")
    private BigDecimal exchangeRate;
    
    @ExcelProperty("回填单号")
    private String remarkOrderNo;
    
    @ExcelProperty("订单备注")
    private String remark;
    
    @ExcelProperty("取消原因")
    private String cancelReason;
    
    @ExcelProperty("取消时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;
    
    @ExcelProperty("确认时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;
    
    @ExcelProperty("完成时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    
    @ExcelProperty("创建人")
    private String createdBy;
    
    @ExcelProperty("更新时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    
    @ExcelProperty("更新人")
    private String updatedBy;
} 