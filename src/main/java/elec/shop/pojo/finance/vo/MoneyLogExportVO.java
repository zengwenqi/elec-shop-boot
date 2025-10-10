package elec.shop.pojo.finance.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 财务日志导出VO
 */
@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER, wrapped = BooleanEnum.TRUE)
public class MoneyLogExportVO {

    @ColumnWidth(15)
    @ExcelIgnore
    private Long logId;

    @ColumnWidth(15)
    @ExcelIgnore
    private Long userId;

    @ColumnWidth(20)
    @ExcelProperty(value = "用户名", index = 0)
    private String username;

    @ColumnWidth(15)
    @ExcelProperty(value = "用户类型", index = 1)
    private String userTypeName;

    @ColumnWidth(15)
    @ExcelProperty(value = "操作类型", index = 2)
    private String operationTypeName;

    @ColumnWidth(15)
    @ExcelProperty(value = "变动金额", index = 3)
    private BigDecimal amount;

    @ColumnWidth(15)
    @ExcelProperty(value = "操作前余额", index = 4)
    private BigDecimal balanceBefore;

    @ColumnWidth(15)
    @ExcelProperty(value = "操作后余额", index = 5)
    private BigDecimal balanceAfter;

    @ColumnWidth(10)
    @ExcelProperty(value = "货币类型", index = 6)
    private String currency;

    @ColumnWidth(25)
    @ExcelProperty(value = "关联订单号", index = 7)
    private String relatedOrderNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "操作描述", index = 8)
    private String description;

    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 9)
    private String remark;

    @ColumnWidth(15)
    @ExcelIgnore
    private Long operatorId;

    @ColumnWidth(20)
    @ExcelProperty(value = "操作员姓名", index = 10)
    private String operatorName;

    @ColumnWidth(20)
    @ExcelProperty(value = "IP地址", index = 11)
    private String ipAddress;

    @ColumnWidth(10)
    @ExcelProperty(value = "状态", index = 12)
    private String statusName;

    @ColumnWidth(25)
    @ExcelProperty(value = "创建时间", index = 13)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ColumnWidth(25)
    @ExcelProperty(value = "更新时间", index = 14)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
