package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 采购任务表
 * @TableName purchase_task
 */
@TableName(value ="purchase_task")
@Data
public class PurchaseTask implements Serializable {
    /**
     * 任务ID
     */
    @TableId
    private Long taskId;

    /**
     * 任务编号
     */
    private String taskCode;

    /**
     * 采购员ID
     */
    private Long purchaserId;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务状态：0-待处理 1-处理中 2-已完成 3-已取消
     */
    private Integer taskStatus;

    /**
     * 优先级：1-低 2-中 3-高
     */
    private Integer priority;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务内容
     */
    private String content;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 完成时间
     */
    private Date completeTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 软删除时间
     */
    private Date deletedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 乐观锁版本
     */
    private Integer version;

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
