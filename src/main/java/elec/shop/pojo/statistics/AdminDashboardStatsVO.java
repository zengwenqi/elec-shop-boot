package elec.shop.pojo.statistics;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员仪表板统计数据VO
 */
@Data
public class AdminDashboardStatsVO {
    
    /**
     * 数据概览卡片
     */
    private OverviewCards overviewCards;
    
    /**
     * 订单趋势数据
     */
    private OrderTrendData orderTrendData;
    
    /**
     * 订单分类统计
     */
    private List<OrderCategoryData> orderCategoryData;
    
    @Data
    public static class OverviewCards {
        /**
         * 总订单数
         */
        private Long totalOrders;
        
        /**
         * 总订单数变化百分比
         */
        private String totalOrdersChange;
        
        /**
         * 待处理订单数
         */
        private Long pendingOrders;
        
        /**
         * 待处理订单变化百分比
         */
        private String pendingOrdersChange;
        
        /**
         * 今日活跃用户数
         */
        private Long todayActiveUsers;
        
        /**
         * 活跃用户变化百分比
         */
        private String activeUsersChange;
        
        /**
         * 系统告警数
         */
        private Long systemAlerts;
        
        /**
         * 系统告警变化百分比
         */
        private String systemAlertsChange;
    }
    
    @Data
    public static class OrderTrendData {
        /**
         * 时间标签
         */
        private List<String> timeLabels;
        
        /**
         * 订单数量数据
         */
        private List<Long> orderCountData;
        
        /**
         * 订单金额数据
         */
        private List<BigDecimal> orderAmountData;
    }
    
    @Data
    public static class OrderCategoryData {
        /**
         * 分类名称
         */
        private String name;
        
        /**
         * 数量
         */
        private Long value;
        
        /**
         * 百分比
         */
        private String percentage;
    }
}