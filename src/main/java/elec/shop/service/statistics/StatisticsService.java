package elec.shop.service.statistics;

import elec.shop.pojo.statistics.DashboardStatsVO;
import elec.shop.pojo.statistics.MonitorDataVO;
import elec.shop.pojo.statistics.AdminDashboardStatsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取仪表板统计数据
     * @return 仪表板统计数据
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 获取实时监控数据
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param type 类型筛选
     * @param status 状态筛选
     * @return 分页监控数据
     */
    IPage<MonitorDataVO> getMonitorData(Integer pageNum, Integer pageSize, String type, String status);

    /**
     * 获取交易趋势数据
     * @param period 时间周期 (week-本周, month-本月, year-本年)
     * @return 交易趋势数据
     */
    Object getTradeTrend(String period);

    /**
     * 获取管理员仪表板统计数据
     * @return 管理员仪表板统计数据
     */
    AdminDashboardStatsVO getAdminDashboardStats();

    /**
     * 获取管理员订单趋势数据
     * @param period 时间周期 (week-本周, month-本月, year-本年)
     * @return 订单趋势数据
     */
    AdminDashboardStatsVO.OrderTrendData getAdminOrderTrend(String period);

    /**
     * 获取系统动态数据
     * @param limit 限制返回的动态数量
     * @return 系统动态数据列表
     */
    Object getSystemActivities(Integer limit);

    // ==================== 采购报表相关方法 ====================

    /**
     * 获取采购报表统计卡片数据
     * @return 采购统计卡片数据
     */
    Object getPurchaseCards();

    /**
     * 获取采购趋势数据
     * @param period 时间周期 (week-本周, month-本月, year-本年)
     * @return 采购趋势数据
     */
    Object getPurchaseTrend(String period);

    /**
     * 获取订单状态分布
     * @return 订单状态分布数据
     */
    Object getOrderStatusDistribution();

    /**
     * 获取商品分类占比
     * @return 商品分类占比数据
     */
    Object getProductCategoryDistribution();

    /**
     * 获取采购平台分布
     * @return 采购平台分布数据
     */
    Object getPurchasePlatformDistribution();

    /**
     * 获取热门采购商品排行
     * @param period 时间周期 (7days, 30days, 90days)
     * @param limit 限制返回的商品数量
     * @return 热门采购商品排行数据
     */
    Object getTopPurchaseProducts(String period, Integer limit);

    /**
     * 获取店铺采购排行
     * @param period 时间周期 (7days, 30days, 90days)
     * @param limit 限制返回的店铺数量
     * @return 店铺采购排行数据
     */
    Object getTopPurchaseShops(String period, Integer limit);

    /**
     * 获取采购员绩效排行
     * @param period 时间周期 (7days, 30days, 90days)
     * @param limit 限制返回的采购员数量
     * @return 采购员绩效排行数据
     */
    Object getTopPurchasers(String period, Integer limit);

    /**
     * 获取工单处理统计
     * @return 工单处理统计数据
     */
    Object getTicketStats();

    // ==================== 采购员个人报表相关方法 ====================

    /**
     * 获取采购员个人统计卡片数据
     * @param purchaserId 采购员ID
     * @return 采购员个人统计卡片数据
     */
    Object getPurchaserCards(Long purchaserId);

    /**
     * 获取采购员订单趋势数据
     * @param purchaserId 采购员ID
     * @param period 时间周期 (week-本周, month-本月, year-本年)
     * @return 采购员订单趋势数据
     */
    Object getPurchaserOrderTrend(Long purchaserId, String period);

    /**
     * 获取采购员平台分布数据
     * @param purchaserId 采购员ID
     * @return 采购员平台分布数据
     */
    Object getPurchaserPlatformDistribution(Long purchaserId);
}
