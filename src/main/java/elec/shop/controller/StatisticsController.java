package elec.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.purchase.PurchaserInfo;
import elec.shop.pojo.statistics.DashboardStatsVO;
import elec.shop.pojo.statistics.MonitorDataVO;
import elec.shop.pojo.statistics.AdminDashboardStatsVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.PurchaserInfoService;
import elec.shop.service.statistics.StatisticsService;
import elec.shop.utils.AllContextUtils;
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
    private final PurchaserInfoService purchaserInfoService;

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

    // ==================== 采购报表相关接口 ====================

    @ApiOperation("获取采购报表统计卡片数据")
    @GetMapping("/purchase-cards")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getPurchaseCards() {
        Object cards = statisticsService.getPurchaseCards();
        return Result.ok(cards);
    }

    @ApiOperation("获取采购趋势数据")
    @GetMapping("/purchase-trend")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getPurchaseTrend(
            @RequestParam(defaultValue = "month") String period) {
        Object trendData = statisticsService.getPurchaseTrend(period);
        return Result.ok(trendData);
    }

    @ApiOperation("获取订单状态分布")
    @GetMapping("/order-status-distribution")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getOrderStatusDistribution() {
        Object distribution = statisticsService.getOrderStatusDistribution();
        return Result.ok(distribution);
    }

    @ApiOperation("获取商品分类占比")
    @GetMapping("/product-category-distribution")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getProductCategoryDistribution() {
        Object distribution = statisticsService.getProductCategoryDistribution();
        return Result.ok(distribution);
    }

    @ApiOperation("获取采购平台分布")
    @GetMapping("/purchase-platform-distribution")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getPurchasePlatformDistribution() {
        Object distribution = statisticsService.getPurchasePlatformDistribution();
        return Result.ok(distribution);
    }

    @ApiOperation("获取热门采购商品排行")
    @GetMapping("/top-purchase-products")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getTopPurchaseProducts(
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        Object products = statisticsService.getTopPurchaseProducts(period, limit);
        return Result.ok(products);
    }

    @ApiOperation("获取店铺采购排行")
    @GetMapping("/top-purchase-shops")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getTopPurchaseShops(
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        Object shops = statisticsService.getTopPurchaseShops(period, limit);
        return Result.ok(shops);
    }

    @ApiOperation("获取采购员绩效排行")
    @GetMapping("/top-purchasers")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getTopPurchasers(
            @RequestParam(defaultValue = "7days") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        Object purchasers = statisticsService.getTopPurchasers(period, limit);
        return Result.ok(purchasers);
    }

    @ApiOperation("获取工单处理统计")
    @GetMapping("/ticket-stats")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getTicketStats() {
        Object stats = statisticsService.getTicketStats();
        return Result.ok(stats);
    }

    // ==================== 采购员采购报表相关接口 ====================

    @ApiOperation("获取采购员个人统计卡片数据")
    @GetMapping("/purchaser-cards")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getPurchaserCards() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        PurchaserInfo one = purchaserInfoService.getOne(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getUserId, loginSysUser.getUserId()));
        if (one==null)
            return Result.fail("无权访问");
        Object cards = statisticsService.getPurchaserCards(one.getPurchaserId());
        return Result.ok(cards);
    }

    @ApiOperation("获取采购员订单趋势数据")
    @GetMapping("/purchaser-order-trend")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getPurchaserOrderTrend(
            @RequestParam(defaultValue = "month") String period) {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        PurchaserInfo one = purchaserInfoService.getOne(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getUserId, loginSysUser.getUserId()));
        if (one==null)
            return Result.fail("无权访问");
        Object trend = statisticsService.getPurchaserOrderTrend(one.getPurchaserId(), period);
        return Result.ok(trend);
    }

    @ApiOperation("获取采购员平台分布数据")
    @GetMapping("/purchaser-platform-distribution")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<?> getPurchaserPlatformDistribution() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        PurchaserInfo one = purchaserInfoService.getOne(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getUserId, loginSysUser.getUserId()));
        if (one==null)
            return Result.fail("无权访问");
        Object distribution = statisticsService.getPurchaserPlatformDistribution(one.getPurchaserId());
        return Result.ok(distribution);
    }
}
