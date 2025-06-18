package elec.shop.pojo.purchase.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("货币账户信息")
public class CurrencyAccountVO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("电子邮箱")
    private String email;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("用户类型：1-超级管理员 2-管理员 3-采购员 4-商户")
    private Integer userType;

    @ApiModelProperty("账户ID")
    private String accountId;

    @ApiModelProperty("账户编号")
    private String accountNo;

    @ApiModelProperty("账户余额")
    private BigDecimal balance;

    @ApiModelProperty("币种")
    private String currency;

    @ApiModelProperty("币种符号")
    private String symbol;

    @ApiModelProperty("汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty("账户类型：1-商户 2-平台")
    private Integer accountType;

    @ApiModelProperty("状态：1-正常 2-冻结 3-注销")
    private Integer status;

    @ApiModelProperty("最后更新时间")
    private Date lastUpdated;
}
