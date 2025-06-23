package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 工单表
 * @TableName service_ticket
 */
@TableName(value ="service_ticket")
@Data
public class ServiceTicket implements Serializable {
    /**
     * 工单ID
     */
    @TableId
    private Long ticketId;

    /**
     * 工单编号
     */
    private String ticketNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 工单类型
     */
    private Integer ticketType;

    /**
     * 优先级：0-普通 1-急需 2-紧急
     */
    private Integer priority;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片列表(JSON)
     */
    private String images;

    /**
     * 状态：0-待处理 1-处理中 2-已解决 3-已关闭
     */
    private Integer status;

    /**
     * 客服ID
     */
    private Long serviceStaffId;

    /**
     * 首次回复时间
     */
    private Date firstReplyTime;

    /**
     * 最后回复时间
     */
    private Date lastReplyTime;

    /**
     * 关闭时间
     */
    private Date closeTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 逻辑删除：0-存在 1-删除，标识记录是否逻辑删除，默认0表示存在
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}