package elec.shop.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import elec.shop.utils.excel.CustomCellStyleStrategy;
import elec.shop.utils.excel.CustomMergeStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ExcelUtils {

    /**
     * 导出Excel（不合并单元格）
     * @param response HttpServletResponse
     * @param data 数据List
     * @param fileName 文件名
     * @param sheetName sheet名
     * @param clazz 实体类
     */
    public static <T> void exportExcel(HttpServletResponse response, List<T> data, String fileName, String sheetName, Class<T> clazz) {
        try {
            setExcelResponseHeader(response, fileName);
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(new CustomCellStyleStrategy())
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
            setExcelResponseHeader(response, fileName);
            // 写入Excel，使用自适应列宽、合并单元格处理器和居中样式
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(new CustomMergeStrategy())
                    .registerWriteHandler(new CustomCellStyleStrategy())
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            log.error("导出Excel异常", e);
            throw new RuntimeException("导出Excel失败");
        }
    }

    /**
     * 设置Excel响应头
     */
    private static void setExcelResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }
}
