package elec.shop.utils.excel;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;

public class CustomCellStyleStrategy extends HorizontalCellStyleStrategy {

    public CustomCellStyleStrategy() {
        super(createHeadStyle(), createContentStyle());
    }

    private static WriteCellStyle createHeadStyle() {
        WriteCellStyle headStyle = new WriteCellStyle();
        // 设置表头样式
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setWrapped(true);
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        return headStyle;
    }

    private static WriteCellStyle createContentStyle() {
        WriteCellStyle contentStyle = new WriteCellStyle();
        // 设置内容样式
        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle.setWrapped(true);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        return contentStyle;
    }
}
