package elec.shop.pojo.announcement;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 跨境服务超市表
 * @TableName cross_border_service
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="cross_border_service")
@Data
public class CrossBorderService extends BaseEntity implements Serializable {
    /**
     * 服务ID
     */
    @TableId
    private Long serviceId;

    /**
     * 服务编码
     */
    private String serviceCode;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务类型
     */
    private Integer serviceType;

    /**
     * 汇率
     */
    private String exchangeRate;

    /**
     * 服务图标URL
     */
    private String serviceIcon;

    /**
     * 服务详情大图URL
     */
    private String serviceImage;

    /**
     * 简短描述
     */
    private String shortDesc;

    /**
     * 服务价格
     */
    private BigDecimal price;

    /**
     * 服务介绍(富文本)
     */
    private String introduction;

    /**
     * 购买须知(JSON数组)
     */
    private String notice;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-下架 1-上架
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
