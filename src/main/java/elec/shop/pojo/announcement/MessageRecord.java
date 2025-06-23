package elec.shop.pojo.announcement;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 消息记录表
 * @TableName message_record
 */
@TableName(value ="message_record")
@Data
public class MessageRecord implements Serializable {
    /**
     * 记录ID
     */
    @TableId
    private Long recordId;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 消息类型：1-短信 2-邮件 3-站内信
     */
    private Integer messageType;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 发送人
     */
    private String sender;

    /**
     * 接收人
     */
    private String receiver;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 发送状态：0-待发送 1-成功 2-失败
     */
    private Integer sendStatus;

    /**
     * 错误信息
     */
    private String errorMsg;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
