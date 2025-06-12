package elec.shop.pojo.purchase.dto;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

/**
 * 店铺信息表
 * @TableName shop_info
 */

@Data
public class ShopInfoDTO {
    /**
     * 店铺ID
     */
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
}
