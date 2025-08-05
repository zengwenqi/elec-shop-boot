package elec.shop.controller;

import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.statistics.DashboardStatsVO;
import elec.shop.pojo.statistics.MonitorDataVO;
import elec.shop.pojo.statistics.AdminDashboardStatsVO;
import elec.shop.service.statistics.StatisticsService;
import elec.shop.utils.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "统计统一接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @ApiOperation("获取仪表板统计数据")
    @GetMapping("/dashboard")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO dashboardStats = statisticsService.getDashboardStats();
        return Result.ok(dashboardStats);
    }

    @ApiOperation("获取实时监控数据")
    @GetMapping("/monitor")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<IPage<MonitorDataVO>> getMonitorData(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        IPage<MonitorDataVO> monitorData = statisticsService.getMonitorData(pageNum, pageSize, type, status);
        return Result.ok(monitorData);
    }

    @ApiOperation("获取交易趋势数据")
    @GetMapping("/trade-trend")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getTradeTrend(
            @RequestParam(defaultValue = "week") String period) {
        // period: week(本周), month(本月), year(本年)
        Object trendData = statisticsService.getTradeTrend(period);
        return Result.ok(trendData);
    }

    // ==================== 管理员控制台预览相关接口 ====================

    @ApiOperation("获取管理员仪表板统计数据")
    @GetMapping("/admin-dashboard")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<AdminDashboardStatsVO> getAdminDashboardStats() {
        AdminDashboardStatsVO adminDashboardStats = statisticsService.getAdminDashboardStats();
        return Result.ok(adminDashboardStats);
    }

    @ApiOperation("获取管理员订单趋势数据")
    @GetMapping("/admin-order-trend")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<AdminDashboardStatsVO.OrderTrendData> getAdminOrderTrend(
            @RequestParam(defaultValue = "week") String period) {
        AdminDashboardStatsVO.OrderTrendData orderTrend = statisticsService.getAdminOrderTrend(period);
        return Result.ok(orderTrend);
    }

    @ApiOperation("获取系统动态数据")
    @GetMapping("/system-activities")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getSystemActivities(
            @RequestParam(defaultValue = "10") Integer limit) {
        Object activities = statisticsService.getSystemActivities(limit);
        return Result.ok(activities);
    }
}
