package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 工单回复表
 * @TableName ticket_reply
 */
@TableName(value ="ticket_reply")
@Data
public class TicketReply implements Serializable {
    /**
     * 回复ID
     */
    @TableId
    private Long replyId;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 回复类型：1-用户 2-客服 3-系统
     */
    private Integer replyType;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 图片列表(JSON)
     */
    private String images;

    /**
     * 回复人ID
     */
    private Long replierId;

    /**
     * 回复人姓名
     */
    private String replierName;

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