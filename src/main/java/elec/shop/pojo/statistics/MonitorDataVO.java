package elec.shop.pojo.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 实时监控数据VO
 */
@Data
@ApiModel("实时监控数据")
public class MonitorDataVO {

    @ApiModelProperty("时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("IP地址")
    private String ip;

    @ApiModelProperty("耗时(毫秒)")
    private Integer duration;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("请求方法")
    private String method;

    @ApiModelProperty("请求路径")
    private String path;

    @ApiModelProperty("响应状态码")
    private Integer responseCode;

    @ApiModelProperty("错误信息")
    private String errorMessage;
}
