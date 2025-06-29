package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("充值反馈DTO")
public class RechargeFeedbackDTO {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("反馈编号")
    private String feedbackNo;

    @ApiModelProperty("充值单号")
    private String rechargeNo;

    @ApiModelProperty("充值金额")
    private BigDecimal money;

    @ApiModelProperty("提交用户ID")
    private Long userId;

    @ApiModelProperty("提交人姓名")
    private String submitter;

    @ApiModelProperty("反馈类型：AMOUNT_NOT_RECEIVED-金额未到账，AMOUNT_ERROR-金额错误，OTHER-其他问题")
    private String type;

    @ApiModelProperty("反馈图片凭证，存储图片路径列表")
    private List<String> images;

    @ApiModelProperty("问题描述")
    private String description;

    @ApiModelProperty("状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已驳回")
    private String status;

    @ApiModelProperty("处理人ID")
    private Long handlerId;

    @ApiModelProperty("处理人姓名")
    private String handler;

    @ApiModelProperty("处理时间")
    private Date handleTime;
}
