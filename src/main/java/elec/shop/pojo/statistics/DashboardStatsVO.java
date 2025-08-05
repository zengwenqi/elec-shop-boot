package elec.shop.pojo.statistics;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 数据仪表板统计VO
 */
@Data
public class DashboardStatsVO {

    /**
     * 数据概览卡片
     */
    private OverviewCards overviewCards;

    /**
     * 交易趋势数据
     */
    private TradeTrendData tradeTrendData;

    /**
     * 用户分布数据
     */
    private List<UserDistributionData> userDistributionData;

    /**
     * 系统状态数据
     */
    private List<SystemStatusData> systemStatusData;

    /**
     * 系统预警数据
     */
    private List<SystemAlertData> systemAlertData;

    /**
     * 实时监控数据
     */
    private List<MonitorData> monitorData;

    @Data
    public static class OverviewCards {
        /**
         * 今日交易额
         */
        private BigDecimal todayTradeAmount;

        /**
         * 交易额变化百分比
         */
        private String tradeAmountChange;

        /**
         * 总交易额
         */
        private BigDecimal totalTradeAmount;

        /**
         * 活跃用户数
         */
        private Integer activeUsers;

        /**
         * 活跃用户变化百分比
         */
        private String activeUsersChange;

        /**
         * 较昨日增长用户数
         */
        private Integer userGrowth;

        /**
         * 订单完成率
         */
        private String orderCompletionRate;

        /**
         * 订单完成率变化百分比
         */
        private String orderCompletionRateChange;

        /**
         * 待处理订单数
         */
        private Integer pendingOrders;

        /**
         * 系统性能评分
         */
        private String systemPerformance;

        /**
         * 系统性能变化百分比
         */
        private String systemPerformanceChange;

        /**
         * 系统运行天数
         */
        private Integer systemRunDays;
    }

    @Data
    public static class TradeTrendData {
        /**
         * 时间标签
         */
        private List<String> timeLabels;

        /**
         * 交易金额数据
         */
        private List<BigDecimal> tradeAmountData;

        /**
         * 订单数量数据
         */
        private List<Integer> orderCountData;
    }

    @Data
    public static class UserDistributionData {
        /**
         * 用户类型名称
         */
        private String name;

        /**
         * 用户数量
         */
        private Integer value;
    }

    @Data
    public static class SystemStatusData {
        /**
         * 状态项名称
         */
        private String name;

        /**
         * 使用率百分比
         */
        private Double value;
    }

    @Data
    public static class SystemAlertData {
        /**
         * 预警ID
         */
        private Long id;

        /**
         * 预警级别 (high/medium/low)
         */
        private String level;

        /**
         * 预警标题
         */
        private String title;

        /**
         * 预警时间
         */
        private String time;

        /**
         * 预警内容
         */
        private String content;
    }

    @Data
    public static class MonitorData {
        /**
         * 时间
         */
        private String time;

        /**
         * 类型
         */
        private String type;

        /**
         * 内容
         */
        private String content;

        /**
         * 状态 (success/error)
         */
        private String status;

        /**
         * IP地址
         */
        private String ip;

        /**
         * 耗时(毫秒)
         */
        private Long duration;
    }
}
