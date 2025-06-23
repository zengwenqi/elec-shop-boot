package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺信息表
 * @TableName shop_info
 */
@TableName(value ="shop_info")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopInfo extends BaseEntity implements Serializable {
    /**
     * 店铺ID
     */
    @TableId
    private Long shopId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺编号
     */
    private String shopCode;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 店铺LOGO路径
     */
    private String shopLogo;

    /**
     * 店铺描述
     */
    private String shopDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
