package elec.shop.pojo.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("邮箱验证请求")
public class EmailVerifyRequest {
    @ApiModelProperty("邮箱地址")
    private String email;

    @ApiModelProperty("验证码")
    private String code;
} 