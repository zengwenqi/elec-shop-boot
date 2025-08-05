package elec.shop.pojo.balance.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("工单返回对象")
public class ServiceTicketVO {

    @ApiModelProperty("工单ID")
    private Long ticketId;

    @ApiModelProperty("工单编号")
    private String ticketNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("联系方式")
    private String mobile;

    @ApiModelProperty("采购员ID")
    private Long purchaseId;

    @ApiModelProperty("采购员编号")
    private String purchaserCode;

    @ApiModelProperty("工单类型")
    private String type;

    @ApiModelProperty("优先级：0-普通 1-急需 2-紧急")
    private Integer priority;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("图片列表")
    private List<String> images;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("客服回复")
    private String reply;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("回复时间")
    private String replyTime;

    @ApiModelProperty("关闭时间")
    private String closeTime;
}
