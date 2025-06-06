package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;

/**
 * 采购员信息表
 * @TableName purchaser_info
 */
@TableName(value ="purchaser_info")
@Data
public class PurchaserInfo extends BaseEntity implements Serializable {
    /**
     * 采购员ID
     */
    @TableId
    private Long purchaserId;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 采购员编号
     */
    private String purchaserCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
