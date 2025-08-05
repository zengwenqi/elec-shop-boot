package elec.shop.job;

import elec.shop.pojo.statistics.AdminDashboardStatsVO;
import elec.shop.pojo.statistics.DashboardStatsVO;
import elec.shop.pojo.statistics.MonitorDataVO;
import elec.shop.service.statistics.StatisticsCacheService;
import elec.shop.service.statistics.StatisticsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计数据缓存定时任务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheTask {

    private final StatisticsService statisticsService;
    private final StatisticsCacheService statisticsCacheService;

    /**
     * 每分钟执行一次，更新超级管理员统计数据缓存
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateStatisticsCache() {
        log.info("开始更新超级管理员统计数据缓存");
        try {
            // 更新仪表板统计数据缓存
            updateDashboardStatsCache();

            // 更新监控数据缓存（常用的几种组合）
//            updateMonitorDataCache();

            // 更新交易趋势数据缓存
            updateTradeTrendCache();

            log.info("超级管理员统计数据缓存更新完成");
        } catch (Exception e) {
            log.error("更新超级管理员统计数据缓存失败", e);
        }
    }

    /**
     * 每分钟执行一次，更新管理员统计数据缓存
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateAdminStatisticsCache() {
        log.info("开始更新管理员统计数据缓存");
        try {
            // 更新仪表板统计数据缓存
            AdminDashboardStatsVO adminDashboardStats = statisticsService.getAdminDashboardStats();
            statisticsCacheService.setAdminDashboardCache(adminDashboardStats);

            // 更新订单趋势数据缓存
            String[] periods = {"week", "month", "year"};

            for (String period : periods) {
                AdminDashboardStatsVO.OrderTrendData adminOrderTrend = statisticsService.getAdminOrderTrend(period);
                statisticsCacheService.setAdminOrderTrendCache(period, adminOrderTrend);
            }

            log.info("管理员统计数据缓存更新完成");
        } catch (Exception e) {
            log.error("更新管理员统计数据缓存失败", e);
        }
    }

    /**
     * 更新仪表板统计数据缓存
     */
    private void updateDashboardStatsCache() {
        try {
            DashboardStatsVO dashboardStats = statisticsService.getDashboardStats();
            statisticsCacheService.setDashboardStatsCache(dashboardStats);
            log.debug("仪表板统计数据缓存更新成功");
        } catch (Exception e) {
            log.error("更新仪表板统计数据缓存失败", e);
        }
    }

    /**
     * 更新监控数据缓存
     */
    private void updateMonitorDataCache() {
        try {
            // 常用的监控数据查询组合
            String[] types = {null, "用户登录", "接口调用", "系统监控"};
            String[] statuses = {null, "success", "error"};
            Integer[] pageSizes = {10, 20, 50};

            for (String type : types) {
                for (String status : statuses) {
                    for (Integer pageSize : pageSizes) {
                        // 只缓存第一页数据
                        IPage<MonitorDataVO> monitorData = statisticsService.getMonitorData(1, pageSize, type, status);
                        statisticsCacheService.setMonitorDataCache(1, pageSize, type, status, monitorData);
                    }
                }
            }
            log.debug("监控数据缓存更新成功");
        } catch (Exception e) {
            log.error("更新监控数据缓存失败", e);
        }
    }

    /**
     * 更新交易趋势数据缓存
     */
    private void updateTradeTrendCache() {
        try {
            String[] periods = {"week", "month", "year"};

            for (String period : periods) {
                Object trendData = statisticsService.getTradeTrend(period);
                statisticsCacheService.setTradeTrendCache(period, trendData);
            }
            log.debug("交易趋势数据缓存更新成功");
        } catch (Exception e) {
            log.error("更新交易趋势数据缓存失败", e);
        }
    }

    /**
     * 手动触发缓存更新（用于测试或紧急情况）
     */
    public void manualUpdateCache() {
        log.info("手动触发统计数据缓存更新");
        updateStatisticsCache();
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        log.info("清除所有统计数据缓存");
        statisticsCacheService.clearAllStatisticsCache();
    }
}
