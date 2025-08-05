package elec.shop.pojo.balance.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("工单查询参数")
public class ServiceTicketQueryDTO {

    @ApiModelProperty("工单类型")
    private String type;

    @ApiModelProperty("处理状态")
    private String status;

    @ApiModelProperty("页码")
    private Integer page = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("采购员ID")
    private Long purchaseId;

    @ApiModelProperty("优先级：0-低 1-中 2-高 3-紧急")
    private Long priority;

    @ApiModelProperty("关键字搜索")
    private String keyword;

    @ApiModelProperty("查询类型：user-用户工单，purchaser-采购员工单，all-所有工单(管理员)")
    private String queryType;
}
