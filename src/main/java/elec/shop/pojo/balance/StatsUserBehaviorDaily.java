package elec.shop.pojo.balance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户行为统计表（按天）
 * @TableName stats_user_behavior_daily
 */
@TableName(value ="stats_user_behavior_daily")
@Data
public class StatsUserBehaviorDaily implements Serializable {
    /**
     * 统计ID
     */
    @TableId
    private Long id;

    /**
     * 统计日期
     */
    private Date statsDate;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    /**
     * 加购次数
     */
    private Integer cartCount;

    /**
     * 下单次数
     */
    private Integer orderCount;

    /**
     * 下单金额
     */
    private BigDecimal orderAmount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 分享次数
     */
    private Integer shareCount;

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