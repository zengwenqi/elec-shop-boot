package elec.shop.pojo.sys.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.util.Date;

/**
 * 系统日志导出VO
 */
@Data
public class SystemLogExportVO {

    @ExcelProperty(value = "日志ID", index = 0)
    private String logId;

    @ExcelProperty(value = "时间", index = 1)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    @ExcelProperty(value = "日志类型", index = 2)
    private String typeName;

    @ExcelProperty(value = "日志级别", index = 3)
    private String levelName;

    @ExcelProperty(value = "日志内容", index = 4)
    private String content;

    @ExcelProperty(value = "操作人", index = 5)
    private String operator;

    @ExcelProperty(value = "IP地址", index = 6)
    private String ip;

    @ExcelProperty(value = "地理位置", index = 7)
    private String location;

    @ExcelProperty(value = "浏览器", index = 8)
    private String browser;

    @ExcelProperty(value = "操作系统", index = 9)
    private String os;

    @ExcelProperty(value = "状态", index = 10)
    private String statusName;

    @ExcelProperty(value = "执行时长(ms)", index = 11)
    private Long time;

    @ExcelProperty(value = "请求方法", index = 12)
    private String method;

    @ExcelProperty(value = "请求参数", index = 13)
    private String params;

    @ExcelProperty(value = "错误信息", index = 14)
    private String stack;
}