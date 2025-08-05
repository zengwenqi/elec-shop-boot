package elec.shop.pojo.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 系统日志查询DTO
 */
@Data
@ApiModel("系统日志查询DTO")
public class SystemLogQueryDTO {
    
    @ApiModelProperty("页码")
    private Integer pageNum = 1;
    
    @ApiModelProperty("页大小")
    private Integer pageSize = 20;
    
    @ApiModelProperty("日志类型：system-系统日志, operation-操作日志, security-安全日志, error-错误日志")
    private String logType;
    
    @ApiModelProperty("日志级别：info, warning, error, critical")
    private String logLevel;
    
    @ApiModelProperty("开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    
    @ApiModelProperty("结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    
    @ApiModelProperty("关键词")
    private String keyword;
    
    @ApiModelProperty("操作人")
    private String operator;
    
    @ApiModelProperty("IP地址")
    private String ip;
    
    @ApiModelProperty("状态：0-失败 1-成功")
    private Integer status;
}