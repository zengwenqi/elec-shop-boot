package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 定时任务表
 * @TableName scheduled_task
 */
@TableName(value ="scheduled_task")
@Data
public class ScheduledTask implements Serializable {
    /**
     * 任务ID
     */
    @TableId
    private Long taskId;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务分组
     */
    private String taskGroup;

    /**
     * 任务类名
     */
    private String taskClass;

    /**
     * 任务方法
     */
    private String taskMethod;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 任务参数(JSON)
     */
    private String params;

    /**
     * 是否并发执行
     */
    private Integer concurrent;

    /**
     * 状态：0-暂停 1-正常
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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