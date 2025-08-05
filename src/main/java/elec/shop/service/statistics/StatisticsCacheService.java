package elec.shop.service.statistics;

import elec.shop.pojo.statistics.DashboardStatsVO;
import elec.shop.pojo.statistics.MonitorDataVO;
import elec.shop.pojo.statistics.AdminDashboardStatsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 统计数据缓存服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis缓存键前缀
    private static final String DASHBOARD_STATS_KEY = "statistics:dashboard";
    private static final String MONITOR_DATA_KEY = "statistics:monitor:";
    private static final String TRADE_TREND_KEY = "statistics:trade_trend:";
    private static final String ADMIN_DASHBOARD_KEY = "statistics:admin_dashboard";
    private static final String ADMIN_ORDER_TREND_KEY = "statistics:admin_order_trend:";

    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRE_MINUTES = 2;

    /**
     * 获取仪表板统计数据缓存
     */
    public DashboardStatsVO getDashboardStatsCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(DASHBOARD_STATS_KEY);
            if (cached != null) {
                return objectMapper.convertValue(cached, DashboardStatsVO.class);
            }
        } catch (Exception e) {
            log.error("获取仪表板统计数据缓存失败", e);
        }
        return null;
    }

    /**
     * 设置仪表板统计数据缓存
     */
    public void setDashboardStatsCache(DashboardStatsVO dashboardStats) {
        try {
            redisTemplate.opsForValue().set(DASHBOARD_STATS_KEY, dashboardStats, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("仪表板统计数据缓存设置成功");
        } catch (Exception e) {
            log.error("设置仪表板统计数据缓存失败", e);
        }
    }

    /**
     * 获取监控数据缓存
     */
    public IPage<MonitorDataVO> getMonitorDataCache(Integer pageNum, Integer pageSize, String type, String status) {
        try {
            String key = buildMonitorDataKey(pageNum, pageSize, type, status);
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.convertValue(cached, new TypeReference<IPage<MonitorDataVO>>() {});
            }
        } catch (Exception e) {
            log.error("获取监控数据缓存失败", e);
        }
        return null;
    }

    /**
     * 设置监控数据缓存
     */
    public void setMonitorDataCache(Integer pageNum, Integer pageSize, String type, String status, IPage<MonitorDataVO> monitorData) {
        try {
            String key = buildMonitorDataKey(pageNum, pageSize, type, status);
            redisTemplate.opsForValue().set(key, monitorData, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("监控数据缓存设置成功: {}", key);
        } catch (Exception e) {
            log.error("设置监控数据缓存失败", e);
        }
    }

    /**
     * 获取交易趋势数据缓存
     */
    public Object getTradeTrendCache(String period) {
        try {
            String key = TRADE_TREND_KEY + period;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.error("获取交易趋势数据缓存失败", e);
        }
        return null;
    }

    /**
     * 设置交易趋势数据缓存
     */
    public void setTradeTrendCache(String period, Object trendData) {
        try {
            String key = TRADE_TREND_KEY + period;
            redisTemplate.opsForValue().set(key, trendData, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("交易趋势数据缓存设置成功: {}", key);
        } catch (Exception e) {
            log.error("设置交易趋势数据缓存失败", e);
        }
    }

    /**
     * 构建监控数据缓存键
     */
    private String buildMonitorDataKey(Integer pageNum, Integer pageSize, String type, String status) {
        StringBuilder keyBuilder = new StringBuilder(MONITOR_DATA_KEY);
        keyBuilder.append(pageNum).append(":").append(pageSize);
        if (type != null && !type.isEmpty()) {
            keyBuilder.append(":").append(type);
        }
        if (status != null && !status.isEmpty()) {
            keyBuilder.append(":").append(status);
        }
        return keyBuilder.toString();
    }

    /**
     * 获取管理员仪表板统计数据缓存
     */
    public AdminDashboardStatsVO getAdminDashboardCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(ADMIN_DASHBOARD_KEY);
            if (cached != null) {
                return objectMapper.convertValue(cached, AdminDashboardStatsVO.class);
            }
        } catch (Exception e) {
            log.error("获取管理员仪表板统计数据缓存失败", e);
        }
        return null;
    }

    /**
     * 设置管理员仪表板统计数据缓存
     */
    public void setAdminDashboardCache(AdminDashboardStatsVO adminDashboardStats) {
        try {
            redisTemplate.opsForValue().set(ADMIN_DASHBOARD_KEY, adminDashboardStats, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("设置管理员仪表板统计数据缓存失败", e);
        }
    }

    /**
     * 获取管理员订单趋势数据缓存
     */
    public AdminDashboardStatsVO.OrderTrendData getAdminOrderTrendCache(String period) {
        try {
            String key = ADMIN_ORDER_TREND_KEY + period;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.convertValue(cached, AdminDashboardStatsVO.OrderTrendData.class);
            }
        } catch (Exception e) {
            log.error("获取管理员订单趋势数据缓存失败", e);
        }
        return null;
    }

    /**
     * 设置管理员订单趋势数据缓存
     */
    public void setAdminOrderTrendCache(String period, AdminDashboardStatsVO.OrderTrendData orderTrendData) {
        try {
            String key = ADMIN_ORDER_TREND_KEY + period;
            redisTemplate.opsForValue().set(key, orderTrendData, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("设置管理员订单趋势数据缓存失败", e);
        }
    }

    /**
     * 清除所有统计数据缓存
     */
    public void clearAllStatisticsCache() {
        try {
            // 清除仪表板缓存
            redisTemplate.delete(DASHBOARD_STATS_KEY);
            
            // 清除监控数据缓存（使用模式匹配）
            redisTemplate.delete(redisTemplate.keys(MONITOR_DATA_KEY + "*"));
            
            // 清除交易趋势缓存
            redisTemplate.delete(redisTemplate.keys(TRADE_TREND_KEY + "*"));
            
            // 清除管理员仪表板缓存
            redisTemplate.delete(ADMIN_DASHBOARD_KEY);
            
            // 清除管理员订单趋势缓存
            redisTemplate.delete(redisTemplate.keys(ADMIN_ORDER_TREND_KEY + "*"));
            
            log.info("所有统计数据缓存清除成功");
        } catch (Exception e) {
            log.error("清除统计数据缓存失败", e);
        }
    }
}