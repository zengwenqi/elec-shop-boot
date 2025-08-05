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
}
