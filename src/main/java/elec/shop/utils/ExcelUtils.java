package elec.shop.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import elec.shop.utils.excel.MergeOrderExcelHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ExcelUtils {

    /**
     * 导出Excel
     * @param response HttpServletResponse
     * @param data 数据List
     * @param fileName 文件名
     * @param sheetName sheet名
     * @param clazz 实体类
     */
    public static <T> void exportExcel(HttpServletResponse response, List<T> data, String fileName, String sheetName, Class<T> clazz) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 写入Excel，使用自适应列宽
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet(sheetName)
                    .doWrite(data);

        } catch (IOException e) {
            log.error("导出Excel异常", e);
            throw new RuntimeException("导出Excel失败");
        }
    }

    /**
     * 导出Excel（支持合并单元格）
     * @param response HttpServletResponse
     * @param data 数据List
     * @param fileName 文件名
     * @param sheetName sheet名
     * @param clazz 实体类
     */
    public static <T> void exportExcelWithMerge(HttpServletResponse response, List<T> data, String fileName, String sheetName, Class<T> clazz) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 写入Excel，使用自适应列宽和合并单元格处理器
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(new MergeOrderExcelHandler())
                    .sheet(sheetName)
                    .doWrite(data);

        } catch (IOException e) {
            log.error("导出Excel异常", e);
        }
    }
}
