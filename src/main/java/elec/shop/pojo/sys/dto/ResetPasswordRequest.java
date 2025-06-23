package elec.shop.pojo.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("重置密码请求")
public class ResetPasswordRequest {
    @ApiModelProperty("邮箱地址")
    private String email;

    @ApiModelProperty("验证码")
    private String code;

    @ApiModelProperty("新密码")
    private String newPassword;
} 