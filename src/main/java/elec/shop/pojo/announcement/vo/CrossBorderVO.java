package elec.shop.pojo.announcement.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("跨境服务VO")
public class CrossBorderVO {

    @ApiModelProperty("服务编码")
    private String serviceCode;

    @ApiModelProperty("服务名称")
    private String serviceName;

    @ApiModelProperty("服务类型")
    private Integer serviceType;

    @ApiModelProperty("服务图标URL")
    private String serviceIcon;

    @ApiModelProperty("服务详情大图URL")
    private String serviceImage;

    @ApiModelProperty("简短描述")
    private String shortDesc;

    @ApiModelProperty("服务价格")
    private BigDecimal price;

    @ApiModelProperty("服务介绍(富文本)")
    private String introduction;

    @ApiModelProperty("购买须知(JSON数组)")
    private String notice;

    @ApiModelProperty("销量")
    private Integer salesCount;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("状态：0-下架 1-上架")
    private Integer status;


}
