package elec.shop.pojo.announcement.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("消息请求参数")
public class MessageRequest {

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("接收人")
    private String receiver;

    @ApiModelProperty("消息类型")
    private Integer messageType;
}
