package elec.shop.pojo.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "分配角色请求参数")
public class AssignRoleDTO {

    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;

    @ApiModelProperty(value = "角色ID列表", required = true)
    private Long roleId;

    @ApiModelProperty(value = "备注", required = true)
    private String remark;
}
