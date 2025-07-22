package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Date;

import elec.shop.handler.JsonNodeTypeHandler;
import lombok.Data;

/**
 * 采购订单暂存表
 * @TableName purchase_order_draft
 */
@TableName(value ="purchase_order_draft")
@Data
public class PurchaseOrderDraft implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 采购平台
     */
    private String crossService;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 收货地址
     */
    private String infoAddress;

    /**
     * 备用地址
     */
    private String infoAddressBei;

    /**
     * 收货人
     */
    private String reciverPerson;

    /**
     * 币种
     */
    private String currency;

    /**
     * 商品订单号
     */
    private String goodsOrderNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 暂存的完整表单数据(JSON格式)
     */
    @TableField(value = "draft_data", typeHandler = JsonNodeTypeHandler.class)
    private JsonNode draftData;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 过期时间
     */
    private Date expireTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
