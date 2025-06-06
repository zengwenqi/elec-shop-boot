package elec.shop.pojo.purchase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import elec.shop.pojo.base.BaseEntity;
import lombok.Data;

/**
 * 采购任务表
 * @TableName purchase_task
 */
@TableName(value ="purchase_task")
@Data
public class PurchaseTask extends BaseEntity implements Serializable {
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


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
