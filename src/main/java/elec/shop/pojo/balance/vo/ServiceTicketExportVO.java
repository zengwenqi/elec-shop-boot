package elec.shop.pojo.balance.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import lombok.Data;

/**
 * 工单导出VO
 */
@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER, wrapped = BooleanEnum.TRUE)
public class ServiceTicketExportVO {
    
    @ColumnWidth(20)
    @ExcelProperty(value = "工单编号", index = 0)
    private String ticketNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "工单标题", index = 1)
    private String title;

    @ColumnWidth(50)
    @ExcelProperty(value = "工单内容", index = 2)
    private String content;

    @ColumnWidth(60)
    @ExcelProperty(value = "工单图片", index = 3)
    private String imagesStr;

    @ColumnWidth(15)
    @ExcelProperty(value = "工单类型", index = 4)
    private String type;

    @ColumnWidth(15)
    @ExcelProperty(value = "处理状态", index = 5)
    private String status;

    @ColumnWidth(15)
    @ExcelProperty(value = "优先级", index = 6)
    private String priority;

    @ColumnWidth(20)
    @ExcelProperty(value = "提交用户", index = 7)
    private String username;

    @ColumnWidth(20)
    @ExcelProperty(value = "联系电话", index = 8)
    private String mobile;

    @ColumnWidth(20)
    @ExcelProperty(value = "采购员编号", index = 9)
    private String purchaserCode;

    @ColumnWidth(25)
    @ExcelProperty(value = "创建时间", index = 10)
    private String createTime;

    @ColumnWidth(25)
    @ExcelProperty(value = "关闭时间", index = 11)
    private String closeTime;
}
