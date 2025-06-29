package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import elec.shop.pojo.base.BaseEntity;
import lombok.Data;

/**
 * 充值反馈表
 * @TableName recharge_feedback
 */
@TableName(value = "recharge_feedback", autoResultMap = true)
@Data
public class RechargeFeedback extends BaseEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 反馈编号
     */
    private String feedbackNo;

    /**
     * 充值单号
     */
    private String rechargeNo;

    /**
     * 充值金额
     */
    private BigDecimal money;

    /**
     * 提交用户ID
     */
    private Long userId;

    /**
     * 提交人姓名
     */
    private String submitter;

    /**
     * 反馈类型：AMOUNT_NOT_RECEIVED-金额未到账，AMOUNT_ERROR-金额错误，OTHER-其他问题
     */
    private String type;

    /**
     * 反馈图片凭证，存储图片路径列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已驳回
     */
    private String status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人姓名
     */
    private String handler;

    /**
     * 处理时间
     */
    private Date handleTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
