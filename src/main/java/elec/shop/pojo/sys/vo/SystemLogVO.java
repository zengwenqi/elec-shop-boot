package elec.shop.pojo.sys.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 系统日志VO
 */
@Data
@ApiModel("系统日志VO")
public class SystemLogVO {

    @ApiModelProperty("日志ID")
    private String logId;

    @ApiModelProperty("时间戳")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    @ApiModelProperty("日志类型：system-系统日志, operation-操作日志, security-安全日志, error-错误日志")
    private String type;

    @ApiModelProperty("日志级别：info, warning, error, critical")
    private String level;

    @ApiModelProperty("日志内容")
    private String content;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("IP地址")
    private String ip;

    @ApiModelProperty("用户代理")
    private String userAgent;

    @ApiModelProperty("堆栈信息")
    private String stack;

    @ApiModelProperty("地理位置")
    private String location;

    @ApiModelProperty("浏览器类型")
    private String browser;

    @ApiModelProperty("操作系统")
    private String os;

    @ApiModelProperty("状态：0-失败 1-成功")
    private Integer status;

    @ApiModelProperty("执行时长(毫秒)")
    private Long time;

    @ApiModelProperty("请求方法")
    private String method;

    @ApiModelProperty("请求参数")
    private String params;
}
