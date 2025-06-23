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
 * 交易统计表（按天）
 * @TableName stats_trade_daily
 */
@TableName(value ="stats_trade_daily")
@Data
public class StatsTradeDaily implements Serializable {
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
     * 商户ID
     */
    private Long merchantId;

    /**
     * 订单总数
     */
    private Integer orderCount;

    /**
     * 订单总金额
     */
    private BigDecimal orderAmount;

    /**
     * 支付订单数
     */
    private Integer paidCount;

    /**
     * 支付总金额
     */
    private BigDecimal paidAmount;

    /**
     * 退款订单数
     */
    private Integer refundCount;

    /**
     * 退款总金额
     */
    private BigDecimal refundAmount;

    /**
     * 下单用户数
     */
    private Integer userCount;

    /**
     * 新增用户数
     */
    private Integer newUserCount;

    /**
     * 币种
     */
    private String currency;

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