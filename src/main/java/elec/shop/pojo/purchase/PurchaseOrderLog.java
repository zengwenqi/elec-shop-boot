package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 采购订单日志表
 * @TableName purchase_order_log
 */
@TableName(value ="purchase_order_log")
@Data
public class PurchaseOrderLog implements Serializable {
    /**
     * 日志ID
     */
    @TableId
    private Long logId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 执行时长(毫秒)
     */
    private Long time;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作说明
     */
    private String operationDesc;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 租户ID
     */
    private Long tenantId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
