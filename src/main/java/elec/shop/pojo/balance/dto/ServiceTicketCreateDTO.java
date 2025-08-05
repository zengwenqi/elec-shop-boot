package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("工单创建请求参数")
public class ServiceTicketCreateDTO {

    @ApiModelProperty(value = "工单类型(0-其他问题 1-技术支持 2-功能建议 3-问题反馈 4-账户问题)", required = true)
    @NotBlank(message = "工单类型不能为空")
    private String type;

    @ApiModelProperty(value = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度应在2到50个字符之间")
    private String title;

    @ApiModelProperty(value = "内容", required = true)
    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 500, message = "内容长度应在10到500个字符之间")
    private String content;

    @ApiModelProperty("图片列表")
    private List<String> images;

    @ApiModelProperty("优先级：0-低 1-中 2-高 3-紧急")
    private Integer priority = 0;

    @ApiModelProperty("关联订单ID")
    private Long orderId;
}
