package elec.shop.service.statistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.mapper.balance.StatsTradeDailyMapper;
import elec.shop.mapper.balance.StatsUserBehaviorDailyMapper;
import elec.shop.mapper.purchase.PurchaseOrderMapper;
import elec.shop.mapper.sys.SysLoginLogMapper;
import elec.shop.mapper.sys.SysOperationLogMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.statistics.DashboardStatsVO;
import elec.shop.pojo.statistics.MonitorDataVO;
import elec.shop.pojo.statistics.AdminDashboardStatsVO;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.pojo.sys.SysLoginLog;
import elec.shop.pojo.sys.SysOperationLog;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.statistics.StatisticsService;
import elec.shop.service.statistics.StatisticsCacheService;

import elec.shop.utils.ServerMonitorUtil;
import elec.shop.utils.SystemPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Random;

/**
 * 统计服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final SysOperationLogMapper sysOperationLogMapper;
    private final StatsTradeDailyMapper statsTradeDailyMapper;
    private final StatsUserBehaviorDailyMapper statsUserBehaviorDailyMapper;
    private final ServerMonitorUtil serverMonitorUtil;
    private final StatisticsCacheService statisticsCacheService;

    @Override
    public DashboardStatsVO getDashboardStats() {
        // 优先从Redis缓存获取数据
        DashboardStatsVO cachedData = statisticsCacheService.getDashboardStatsCache();
        if (cachedData != null) {
            return cachedData;
        }

        // 缓存中没有数据，走原来的策略
        DashboardStatsVO dashboardStats = new DashboardStatsVO();

        // 获取数据概览卡片
        dashboardStats.setOverviewCards(getOverviewCards());

        // 获取用户分布数据
        dashboardStats.setUserDistributionData(getUserDistributionData());

        // 获取系统状态数据
        dashboardStats.setSystemStatusData(getSystemStatusData());

        // 获取系统预警数据
        dashboardStats.setSystemAlertData(getSystemAlertData());

        // 将数据存入缓存
        statisticsCacheService.setDashboardStatsCache(dashboardStats);

        return dashboardStats;
    }

    /**
     * 获取数据概览卡片
     */
    private DashboardStatsVO.OverviewCards getOverviewCards() {
        DashboardStatsVO.OverviewCards cards = new DashboardStatsVO.OverviewCards();

        // 今日交易额统计
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrowStart = cal.getTime();

        // 今日订单统计
        LambdaQueryWrapper<PurchaseOrder> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.between(PurchaseOrder::getCreatedAt, todayStart, tomorrowStart)
                   .eq(PurchaseOrder::getPaymentStatus, 1); // 已支付
        List<PurchaseOrder> todayOrders = purchaseOrderMapper.selectList(todayWrapper);

        BigDecimal todayAmount = todayOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.setTodayTradeAmount(todayAmount);

        // 昨日交易额对比
        cal.setTime(todayStart);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterdayStart = cal.getTime();

        LambdaQueryWrapper<PurchaseOrder> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.between(PurchaseOrder::getCreatedAt, yesterdayStart, todayStart)
                       .eq(PurchaseOrder::getPaymentStatus, 1);
        List<PurchaseOrder> yesterdayOrders = purchaseOrderMapper.selectList(yesterdayWrapper);

        BigDecimal yesterdayAmount = yesterdayOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (yesterdayAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal changeRate = todayAmount.subtract(yesterdayAmount)
                    .divide(yesterdayAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            cards.setTradeAmountChange((changeRate.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") +
                    changeRate.setScale(1, RoundingMode.HALF_UP) + "%");
        } else {
            cards.setTradeAmountChange("+100.0%");
        }

        // 总交易额
        LambdaQueryWrapper<PurchaseOrder> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(PurchaseOrder::getPaymentStatus, 1);
        List<PurchaseOrder> allOrders = purchaseOrderMapper.selectList(totalWrapper);
        BigDecimal totalAmount = allOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.setTotalTradeAmount(totalAmount);

        // 活跃用户统计（今日登录用户）
        LambdaQueryWrapper<SysLoginLog> loginWrapper = new LambdaQueryWrapper<>();
        loginWrapper.between(SysLoginLog::getLoginTime, todayStart, tomorrowStart)
                   .eq(SysLoginLog::getStatus, 1);
        List<SysLoginLog> todayLogins = sysLoginLogMapper.selectList(loginWrapper);
        Set<Long> activeUserIds = todayLogins.stream()
                .map(SysLoginLog::getUserId)
                .collect(Collectors.toSet());
        cards.setActiveUsers(activeUserIds.size());

        // 昨日活跃用户对比
        LambdaQueryWrapper<SysLoginLog> yesterdayLoginWrapper = new LambdaQueryWrapper<>();
        yesterdayLoginWrapper.between(SysLoginLog::getLoginTime, yesterdayStart, todayStart)
                            .eq(SysLoginLog::getStatus, 1);
        List<SysLoginLog> yesterdayLogins = sysLoginLogMapper.selectList(yesterdayLoginWrapper);
        Set<Long> yesterdayActiveUserIds = yesterdayLogins.stream()
                .map(SysLoginLog::getUserId)
                .collect(Collectors.toSet());

        int userGrowth = activeUserIds.size() - yesterdayActiveUserIds.size();
        cards.setUserGrowth(userGrowth);

        if (yesterdayActiveUserIds.size() > 0) {
            double userChangeRate = (double) userGrowth / yesterdayActiveUserIds.size() * 100;
            cards.setActiveUsersChange((userChangeRate >= 0 ? "+" : "") +
                    String.format("%.1f", userChangeRate) + "%");
        } else {
            cards.setActiveUsersChange("+100.0%");
        }

        // 订单完成率
        LambdaQueryWrapper<PurchaseOrder> allTodayOrdersWrapper = new LambdaQueryWrapper<>();
        allTodayOrdersWrapper.between(PurchaseOrder::getCreatedAt, todayStart, tomorrowStart);
        Long totalTodayOrders = purchaseOrderMapper.selectCount(allTodayOrdersWrapper);

        LambdaQueryWrapper<PurchaseOrder> completedOrdersWrapper = new LambdaQueryWrapper<>();
        completedOrdersWrapper.between(PurchaseOrder::getCreatedAt, todayStart, tomorrowStart)
                             .eq(PurchaseOrder::getOrderStatus, 4); // 已完成
        Long completedOrders = purchaseOrderMapper.selectCount(completedOrdersWrapper);

        if (totalTodayOrders > 0) {
            double completionRate = (double) completedOrders / totalTodayOrders * 100;
            cards.setOrderCompletionRate(String.format("%.1f", completionRate) + "%");
        } else {
            cards.setOrderCompletionRate("0.0%");
        }

        // 待处理订单
        LambdaQueryWrapper<PurchaseOrder> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.in(PurchaseOrder::getOrderStatus, Arrays.asList(1, 2, 3)); // 待支付、已支付、处理中
        Long pendingCount = purchaseOrderMapper.selectCount(pendingWrapper);
        cards.setPendingOrders(Math.toIntExact(pendingCount));

        // 系统性能（模拟数据）
        cards.setSystemPerformance(SystemPerformanceService.getSystemPerformance());
        cards.setSystemPerformanceChange("");

        // 系统运行天数（从第一个订单开始计算）
        LambdaQueryWrapper<PurchaseOrder> firstOrderWrapper = new LambdaQueryWrapper<>();
        firstOrderWrapper.orderByAsc(PurchaseOrder::getCreatedAt).last("LIMIT 1");
        PurchaseOrder firstOrder = purchaseOrderMapper.selectOne(firstOrderWrapper);
        if (firstOrder != null && firstOrder.getCreatedAt() != null) {
            LocalDate firstDate = firstOrder.getCreatedAt().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(firstDate, currentDate);
            cards.setSystemRunDays((int) daysBetween);
        } else {
            cards.setSystemRunDays(0);
        }

        return cards;
    }

    /**
     * 获取交易趋势数据（最近7天）
     */
    private DashboardStatsVO.TradeTrendData getTradeTrendData() {
        DashboardStatsVO.TradeTrendData trendData = new DashboardStatsVO.TradeTrendData();

        List<String> timeLabels = new ArrayList<>();
        List<BigDecimal> tradeAmountData = new ArrayList<>();
        List<Integer> orderCountData = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6); // 从6天前开始

        for (int i = 0; i < 7; i++) {
            Date dayStart = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date dayEnd = cal.getTime();

            // 格式化日期标签
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            timeLabels.add(sdf.format(dayStart));

            // 查询当天订单数据
            LambdaQueryWrapper<PurchaseOrder> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.between(PurchaseOrder::getCreatedAt, dayStart, dayEnd)
                     .eq(PurchaseOrder::getPaymentStatus, 1);
            List<PurchaseOrder> dayOrders = purchaseOrderMapper.selectList(dayWrapper);

            BigDecimal dayAmount = dayOrders.stream()
                    .map(PurchaseOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            tradeAmountData.add(dayAmount);
            orderCountData.add(dayOrders.size());
        }

        trendData.setTimeLabels(timeLabels);
        trendData.setTradeAmountData(tradeAmountData);
        trendData.setOrderCountData(orderCountData);

        return trendData;
    }

    /**
     * 获取用户分布数据
     */
    private List<DashboardStatsVO.UserDistributionData> getUserDistributionData() {
        List<DashboardStatsVO.UserDistributionData> distributionData = new ArrayList<>();

        // 按用户类型统计
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getStatus, 1); // 正常状态
        List<SysUser> allUsers = sysUserMapper.selectList(userWrapper);

        Map<Integer, Long> userTypeCount = allUsers.stream()
                .collect(Collectors.groupingBy(SysUser::getUserType, Collectors.counting()));

        // 用户类型映射
        Map<Integer, String> userTypeMap = new HashMap<>();
        userTypeMap.put(1, "超级管理员");
        userTypeMap.put(2, "管理员");
        userTypeMap.put(3, "采购员");
        userTypeMap.put(4, "用户");

        for (Map.Entry<Integer, Long> entry : userTypeCount.entrySet()) {
            DashboardStatsVO.UserDistributionData data = new DashboardStatsVO.UserDistributionData();
            data.setName(userTypeMap.getOrDefault(entry.getKey(), "未知"));
            data.setValue(entry.getValue().intValue());
            distributionData.add(data);
        }

        return distributionData;
    }

    /**
     * 获取系统状态数据
     */
    private List<DashboardStatsVO.SystemStatusData> getSystemStatusData() {
        List<DashboardStatsVO.SystemStatusData> statusData = new ArrayList<>();

        try {
            // CPU使用率
            DashboardStatsVO.SystemStatusData cpuData = new DashboardStatsVO.SystemStatusData();
            cpuData.setName("CPU使用率");
            cpuData.setValue((Double) serverMonitorUtil.getCpuInfo().get("usage"));
            statusData.add(cpuData);

            // 内存使用率
            DashboardStatsVO.SystemStatusData memoryData = new DashboardStatsVO.SystemStatusData();
            memoryData.setName("内存使用率");
            memoryData.setValue((Double) serverMonitorUtil.getMemoryInfo().get("usage"));
            statusData.add(memoryData);

            // 磁盘使用率
            DashboardStatsVO.SystemStatusData diskData = new DashboardStatsVO.SystemStatusData();
            diskData.setName("磁盘使用率");
            diskData.setValue((Double) serverMonitorUtil.getDiskInfo().get("usage"));
            statusData.add(diskData);

            // 带宽使用率
            DashboardStatsVO.SystemStatusData bandwidthData = new DashboardStatsVO.SystemStatusData();
            bandwidthData.setName("带宽速率");
            bandwidthData.setValue((Double) serverMonitorUtil.getNetworkInfo().get("downloadSpeedMB"));
            statusData.add(bandwidthData);

        } catch (Exception e) {
            log.info("获取系统信息失败：" + e.getMessage());
        }

        return statusData;
    }

    /**
     * 获取系统预警数据
     */
    private List<DashboardStatsVO.SystemAlertData> getSystemAlertData() {
        List<DashboardStatsVO.SystemAlertData> alertData = new ArrayList<>();

        // 检查数据库连接数（模拟）
        DashboardStatsVO.SystemAlertData dbAlert = new DashboardStatsVO.SystemAlertData();
        dbAlert.setId(1L);
        dbAlert.setLevel("high");
        dbAlert.setTitle("数据库连接数过高");
        dbAlert.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dbAlert.setContent("当前连接数超过预警阈值");
        alertData.add(dbAlert);

        // 检查异常登录（基于登录日志）
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600000);
        LambdaQueryWrapper<SysLoginLog> loginWrapper = new LambdaQueryWrapper<>();
        loginWrapper.gt(SysLoginLog::getLoginTime, oneHourAgo)
                   .eq(SysLoginLog::getStatus, 0); // 登录失败
        Long failedLogins = sysLoginLogMapper.selectCount(loginWrapper);

        if (failedLogins > 10) {
            DashboardStatsVO.SystemAlertData loginAlert = new DashboardStatsVO.SystemAlertData();
            loginAlert.setId(2L);
            loginAlert.setLevel("medium");
            loginAlert.setTitle("异常登录检测");
            loginAlert.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            loginAlert.setContent("检测到大量登录失败尝试");
            alertData.add(loginAlert);
        }

        // 系统更新提醒（模拟）
        DashboardStatsVO.SystemAlertData updateAlert = new DashboardStatsVO.SystemAlertData();
        updateAlert.setId(3L);
        updateAlert.setLevel("low");
        updateAlert.setTitle("系统更新提醒");
        updateAlert.setTime(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        updateAlert.setContent("新版本可用");
        alertData.add(updateAlert);

        return alertData;
    }

    @Override
    public IPage<MonitorDataVO> getMonitorData(Integer pageNum, Integer pageSize, String type, String status) {
        // 优先从Redis缓存获取数据
//        IPage<MonitorDataVO> cachedData = statisticsCacheService.getMonitorDataCache(pageNum, pageSize, type, status);
//        if (cachedData != null) {
//            return cachedData;
//        }

        // 缓存中没有数据，走原来的策略
        List<MonitorDataVO> allData = new ArrayList<>();

        // 根据类型筛选数据源
//        if ("用户登录".equals(type)) {
            // 获取登录日志数据 - 使用分页查询
            Page<SysLoginLog> loginPage = new Page<>(1, 500); // 获取最近500条登录记录
            IPage<SysLoginLog> loginLogs = sysLoginLogMapper.selectPage(loginPage,
                new LambdaQueryWrapper<SysLoginLog>()
                    .orderByDesc(SysLoginLog::getLoginTime)
            );

            for (SysLoginLog login : loginLogs.getRecords()) {
                MonitorDataVO data = new MonitorDataVO();
                data.setTime(login.getLoginTime());
                data.setType("用户登录");
                data.setContent("用户 " + login.getUsername() + " 登录系统");
                data.setStatus(login.getStatus()==1 ? "success" : "error");
                data.setIp(login.getIp());
                data.setDuration(login.getTime() != null ? login.getTime().intValue() : 50 + new Random().nextInt(200));
                data.setUserId(login.getUserId());
                data.setUsername(login.getUsername());
                data.setMethod("POST");
                data.setPath("/auth/login");
                data.setResponseCode(200);

                allData.add(data);
            }
//        }else if (type!=null&&!type.equals("用户登录")) {
            // 获取操作日志数据 - 使用分页查询
//            Page<SysOperationLog> operationPage = new Page<>(1, 500); // 获取最近500条操作记录
//            IPage<SysOperationLog> operationLogs = sysOperationLogMapper.selectPage(operationPage,
//                new LambdaQueryWrapper<SysOperationLog>()
//                        .eq(SysOperationLog::getOperationType, type)
//                    .orderByDesc(SysOperationLog::getCreatedAt)
//            );
//
//            for (SysOperationLog operation : operationLogs.getRecords()) {
//                MonitorDataVO data = new MonitorDataVO();
//                data.setTime(operation.getCreatedAt());
//                data.setType(operation.getOperationType());
//                data.setContent(operation.getOperationType() + ": " + operation.getMethod());
//                data.setStatus(operation.getStatus()==1 ? "success" : "error");
//                data.setIp(operation.getIp());
//                data.setDuration(operation.getTime() != null ? operation.getTime().intValue() : 50 + new Random().nextInt(200));
//                data.setUserId(operation.getUserId());
//                data.setUsername(operation.getUsername());
//                data.setMethod(operation.getMethod());
//                data.setPath(operation.getMethod());
//                data.setResponseCode(200);
//
//                allData.add(data);
//            }
//        }else {
            // 获取操作日志数据 - 使用分页查询
            Page<SysOperationLog> operationPage = new Page<>(1, 500); // 获取最近500条操作记录
            IPage<SysOperationLog> operationLogs = sysOperationLogMapper.selectPage(operationPage,
                    new LambdaQueryWrapper<SysOperationLog>()
                            .orderByDesc(SysOperationLog::getCreatedAt)
            );

            for (SysOperationLog operation : operationLogs.getRecords()) {
                MonitorDataVO data = new MonitorDataVO();
                data.setTime(operation.getCreatedAt());
                data.setType(operation.getOperationType());
                data.setContent(operation.getOperationType() + ": " + operation.getMethod());
                data.setStatus(operation.getStatus()==1?"success":"error");
                data.setIp(operation.getIp());
                data.setDuration(operation.getTime() != null ? operation.getTime().intValue() : 50 + new Random().nextInt(200));
                data.setUserId(operation.getUserId());
                data.setUsername(operation.getUsername());
                data.setMethod(operation.getMethod());
                data.setPath(operation.getMethod());
                data.setResponseCode(200);

                allData.add(data);
            }
//        }

        // 按时间排序
        allData.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        // 使用MyBatis-Plus风格的分页处理
        long total = allData.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allData.size());

        List<MonitorDataVO> pageData = new ArrayList<>();
        if (startIndex < total && startIndex >= 0) {
            pageData = allData.subList(startIndex, endIndex);
        }

        // 创建IPage对象
        IPage<MonitorDataVO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageData);

        // 将数据存入缓存
//        statisticsCacheService.setMonitorDataCache(pageNum, pageSize, type, status, page);

        return page;
    }

    @Override
    public Object getTradeTrend(String period) {
        // 优先从Redis缓存获取数据
        Object cachedData = statisticsCacheService.getTradeTrendCache(period);
        if (cachedData != null) {
            return cachedData;
        }

        // 缓存中没有数据，走原来的策略
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        String timeFormat;

        // 根据时间周期设置查询起始时间和时间格式
        switch (period) {
            case "week":
                startTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "MM-dd";
                break;
            case "month":
                startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "MM-dd";
                break;
            case "year":
                startTime = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "yyyy-MM";
                break;
            default:
                startTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "MM-dd";
                break;
        }

        // 转换为Date类型用于MyBatis查询
        Date startDate = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        // 查询指定时间段内的已支付订单
        LambdaQueryWrapper<PurchaseOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.between(PurchaseOrder::getCreatedAt, startDate, endDate)
                   .eq(PurchaseOrder::getPaymentStatus, 1) // 已支付
                   .orderByAsc(PurchaseOrder::getCreatedAt);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(orderWrapper);

        // 按日期分组统计
        Map<String, List<PurchaseOrder>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    LocalDateTime orderTime = order.getCreatedAt().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    return orderTime.format(DateTimeFormatter.ofPattern(timeFormat));
                }));

        // 准备返回数据
        DashboardStatsVO.TradeTrendData trendData = new DashboardStatsVO.TradeTrendData();
        List<String> timeLabels = new ArrayList<>();
        List<BigDecimal> tradeAmountData = new ArrayList<>();
        List<Integer> orderCountData = new ArrayList<>();

        // 生成完整的时间序列
        LocalDateTime current = startTime;
        while (!current.isAfter(now)) {
            String timeLabel = current.format(DateTimeFormatter.ofPattern(timeFormat));
            timeLabels.add(timeLabel);

            // 获取当前时间点的订单数据
            List<PurchaseOrder> dayOrders = ordersByDate.getOrDefault(timeLabel, new ArrayList<>());

            // 计算交易金额
            BigDecimal dayAmount = dayOrders.stream()
                    .map(PurchaseOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            tradeAmountData.add(dayAmount);
            orderCountData.add(dayOrders.size());

            // 根据时间周期增加时间
            switch (period) {
                case "week":
                case "month":
                    current = current.plusDays(1);
                    break;
                case "year":
                    current = current.plusMonths(1);
                    break;
            }
        }

        trendData.setTimeLabels(timeLabels);
        trendData.setTradeAmountData(tradeAmountData);
        trendData.setOrderCountData(orderCountData);
        // 将数据存入缓存
        statisticsCacheService.setTradeTrendCache(period, trendData);
        return trendData;
    }

    @Override
    public AdminDashboardStatsVO getAdminDashboardStats() {
        // 尝试从缓存获取数据
        AdminDashboardStatsVO cachedData = statisticsCacheService.getAdminDashboardCache();
        if (cachedData != null) {
            return cachedData;
        }

        AdminDashboardStatsVO adminStats = new AdminDashboardStatsVO();

        // 获取概览卡片数据
        adminStats.setOverviewCards(getAdminOverviewCards());

        // 获取订单趋势数据（默认本周）
        adminStats.setOrderTrendData(getAdminOrderTrend("week"));

        // 获取订单分类统计
        adminStats.setOrderCategoryData(getOrderCategoryData());

        // 将数据存入缓存（缓存5分钟）
        statisticsCacheService.setAdminDashboardCache(adminStats);

        return adminStats;
    }

    @Override
    public AdminDashboardStatsVO.OrderTrendData getAdminOrderTrend(String period) {
        // 尝试从缓存获取数据
        AdminDashboardStatsVO.OrderTrendData cachedData = statisticsCacheService.getAdminOrderTrendCache(period);
        if (cachedData != null) {
            return cachedData;
        }

        // 参照getTradeTrend方法的查询逻辑
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        String timeFormat;

        // 根据时间周期设置查询起始时间和时间格式（与getTradeTrend保持一致）
        switch (period) {
            case "week":
                startTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "MM-dd";
                break;
            case "month":
                startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "MM-dd";
                break;
            case "year":
                startTime = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "yyyy-MM";
                break;
            default:
                startTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                timeFormat = "MM-dd";
                break;
        }

        // 转换为Date类型用于MyBatis查询
        Date startDate = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        // 查询指定时间段内的订单（参照getTradeTrend的查询条件）
        LambdaQueryWrapper<PurchaseOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.between(PurchaseOrder::getCreatedAt, startDate, endDate)
                   .orderByAsc(PurchaseOrder::getCreatedAt);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(orderWrapper);

        // 按日期分组统计（与getTradeTrend保持一致的分组逻辑）
        Map<String, List<PurchaseOrder>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    LocalDateTime orderTime = order.getCreatedAt().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    return orderTime.format(DateTimeFormatter.ofPattern(timeFormat));
                }));

        // 准备返回数据
        AdminDashboardStatsVO.OrderTrendData trendData = new AdminDashboardStatsVO.OrderTrendData();
        List<String> timeLabels = new ArrayList<>();
        List<Long> orderCountData = new ArrayList<>();
        List<BigDecimal> orderAmountData = new ArrayList<>();

        // 生成完整的时间序列（与getTradeTrend保持一致）
        LocalDateTime current = startTime;
        while (!current.isAfter(now)) {
            String timeLabel = current.format(DateTimeFormatter.ofPattern(timeFormat));
            timeLabels.add(timeLabel);

            // 获取当前时间点的订单数据
            List<PurchaseOrder> dayOrders = ordersByDate.getOrDefault(timeLabel, new ArrayList<>());

            // 计算订单数量
            orderCountData.add((long) dayOrders.size());

            // 计算交易金额（使用realTotalAmount字段）
            BigDecimal dayAmount = dayOrders.stream()
                    .map(PurchaseOrder::getRealTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderAmountData.add(dayAmount);

            // 根据时间周期增加时间（与getTradeTrend保持一致）
            switch (period) {
                case "week":
                case "month":
                    current = current.plusDays(1);
                    break;
                case "year":
                    current = current.plusMonths(1);
                    break;
            }
        }

        trendData.setTimeLabels(timeLabels);
        trendData.setOrderCountData(orderCountData);
        trendData.setOrderAmountData(orderAmountData);

        // 将数据存入缓存
        statisticsCacheService.setAdminOrderTrendCache(period, trendData);

        return trendData;
    }

    /**
     * 获取管理员概览卡片数据
     */
    private AdminDashboardStatsVO.OverviewCards getAdminOverviewCards() {
        AdminDashboardStatsVO.OverviewCards cards = new AdminDashboardStatsVO.OverviewCards();

        // 获取总订单数
        Long totalOrders = purchaseOrderMapper.selectCount(null);
        cards.setTotalOrders(totalOrders);

        // 计算总订单数变化（与上周对比）
        LocalDate today = LocalDate.now();
        LocalDate lastWeekStart = today.minusDays(13);
        LocalDate lastWeekEnd = today.minusDays(7);
        LocalDate thisWeekStart = today.minusDays(6);

        Date lastWeekStartDate = Date.from(lastWeekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastWeekEndDate = Date.from(lastWeekEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date thisWeekStartDate = Date.from(thisWeekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEndDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<PurchaseOrder> lastWeekQuery = new LambdaQueryWrapper<>();
        lastWeekQuery.between(PurchaseOrder::getCreatedAt, lastWeekStartDate, lastWeekEndDate);
        Long lastWeekOrders = purchaseOrderMapper.selectCount(lastWeekQuery);

        LambdaQueryWrapper<PurchaseOrder> thisWeekQuery = new LambdaQueryWrapper<>();
        thisWeekQuery.between(PurchaseOrder::getCreatedAt, thisWeekStartDate, todayEndDate);
        Long thisWeekOrders = purchaseOrderMapper.selectCount(thisWeekQuery);

        String totalOrdersChange = calculateChangePercentage(lastWeekOrders, thisWeekOrders);
        cards.setTotalOrdersChange(totalOrdersChange);

        // 获取待处理订单数（状态为待处理的订单）
        LambdaQueryWrapper<PurchaseOrder> pendingQuery = new LambdaQueryWrapper<>();
        pendingQuery.eq(PurchaseOrder::getOrderStatus, 1); // 假设1为待处理状态
        Long pendingOrders = purchaseOrderMapper.selectCount(pendingQuery);
        cards.setPendingOrders(pendingOrders);

        // 计算待处理订单变化
        LambdaQueryWrapper<PurchaseOrder> lastWeekPendingQuery = new LambdaQueryWrapper<>();
        lastWeekPendingQuery.eq(PurchaseOrder::getOrderStatus, 1)
                .between(PurchaseOrder::getCreatedAt, lastWeekStartDate, lastWeekEndDate);
        Long lastWeekPending = purchaseOrderMapper.selectCount(lastWeekPendingQuery);

        LambdaQueryWrapper<PurchaseOrder> thisWeekPendingQuery = new LambdaQueryWrapper<>();
        thisWeekPendingQuery.eq(PurchaseOrder::getOrderStatus, 1)
                .between(PurchaseOrder::getCreatedAt, thisWeekStartDate, todayEndDate);
        Long thisWeekPending = purchaseOrderMapper.selectCount(thisWeekPendingQuery);

        String pendingOrdersChange = calculateChangePercentage(lastWeekPending, thisWeekPending);
        cards.setPendingOrdersChange(pendingOrdersChange);

        // 获取今日活跃用户数（今日有登录记录的用户）
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LambdaQueryWrapper<SysLoginLog> todayLoginQuery = new LambdaQueryWrapper<>();
        todayLoginQuery.between(SysLoginLog::getLoginTime, todayStart, todayEndDate);
        List<SysLoginLog> todayLogins = sysLoginLogMapper.selectList(todayLoginQuery);
        Long todayActiveUsers = todayLogins.stream()
                .map(SysLoginLog::getUserId)
                .distinct()
                .count();
        cards.setTodayActiveUsers(todayActiveUsers);

        // 计算活跃用户变化（与昨日对比）
        Date yesterdayStart = Date.from(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        LambdaQueryWrapper<SysLoginLog> yesterdayLoginQuery = new LambdaQueryWrapper<>();
        yesterdayLoginQuery.between(SysLoginLog::getLoginTime, yesterdayStart, todayStart);
        List<SysLoginLog> yesterdayLogins = sysLoginLogMapper.selectList(yesterdayLoginQuery);
        Long yesterdayActiveUsers = yesterdayLogins.stream()
                .map(SysLoginLog::getUserId)
                .distinct()
                .count();

        String activeUsersChange = calculateChangePercentage(yesterdayActiveUsers, todayActiveUsers);
        cards.setActiveUsersChange(activeUsersChange);

        // 获取系统告警数（今日错误日志数量）
        LambdaQueryWrapper<SysOperationLog> alertQuery = new LambdaQueryWrapper<>();
        alertQuery.isNotNull(SysOperationLog::getErrorMsg)
                .between(SysOperationLog::getCreatedAt, todayStart, todayEndDate);
        Long systemAlerts = sysOperationLogMapper.selectCount(alertQuery);
        cards.setSystemAlerts(systemAlerts);

        // 计算系统告警变化（与昨日对比）
        LambdaQueryWrapper<SysOperationLog> yesterdayAlertQuery = new LambdaQueryWrapper<>();
        yesterdayAlertQuery.isNotNull(SysOperationLog::getErrorMsg)
                .between(SysOperationLog::getCreatedAt, yesterdayStart, todayStart);
        Long yesterdayAlerts = sysOperationLogMapper.selectCount(yesterdayAlertQuery);

        String systemAlertsChange = calculateChangePercentage(yesterdayAlerts, systemAlerts);
        cards.setSystemAlertsChange(systemAlertsChange);

        return cards;
    }

    /**
     * 获取订单分类统计数据
     */
    private List<AdminDashboardStatsVO.OrderCategoryData> getOrderCategoryData() {
        // 使用LambdaQueryWrapper查询所有订单，然后在Java代码中进行分组统计
        LambdaQueryWrapper<PurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PurchaseOrder::getOrderType);

        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(queryWrapper);

        // 按订单类型分组统计
        Map<Integer, Long> typeCountMap = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderType() != null ? order.getOrderType() : 0,
                        Collectors.counting()
                ));

        // 计算总数
        long totalCount = typeCountMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return typeCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .map(entry -> {
                    AdminDashboardStatsVO.OrderCategoryData categoryData = new AdminDashboardStatsVO.OrderCategoryData();
                    Integer orderType = entry.getKey();
                    Long count = entry.getValue();

                    // 根据订单类型设置名称
                    String typeName = getOrderTypeName(orderType);
                    categoryData.setName(typeName);
                    categoryData.setValue(count);

                    // 计算百分比
                    if (totalCount > 0) {
                        BigDecimal percentage = BigDecimal.valueOf(count)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
                        categoryData.setPercentage(percentage + "%");
                    } else {
                        categoryData.setPercentage("0%");
                    }

                    return categoryData;
                }).collect(Collectors.toList());
    }

    /**
     * 计算变化百分比
     */
    private String calculateChangePercentage(Long oldValue, Long newValue) {
        if (oldValue == null || oldValue == 0) {
            return newValue > 0 ? "+100%" : "0%";
        }

        BigDecimal change = BigDecimal.valueOf(newValue - oldValue)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(oldValue), 1, RoundingMode.HALF_UP);

        return (change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + change + "%";
    }

    /**
     * 获取订单类型名称
     */
    private String getOrderTypeName(Integer orderType) {
        if (orderType == null) {
            return "未知类型";
        }

        switch (orderType) {
            case 1:
                return "普通订单";
            case 2:
                return "预售订单";
            case 3:
                return "团购订单";
            case 4:
                return "秒杀订单";
            default:
                return "其他类型";
        }
    }

    @Override
    public Object getSystemActivities(Integer limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // 获取最近的登录日志
            LambdaQueryWrapper<SysLoginLog> loginQuery = new LambdaQueryWrapper<>();
            loginQuery.eq(SysLoginLog::getStatus, 1) // 只获取成功登录的记录
                    .orderByDesc(SysLoginLog::getLoginTime)
                    .last("LIMIT " + (limit / 2)); // 登录日志占一半
            
            List<SysLoginLog> loginLogs = sysLoginLogMapper.selectList(loginQuery);
            
            for (SysLoginLog loginLog : loginLogs) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("content", "用户 " + loginLog.getUsername() + " 登录系统");
                activity.put("time", formatDateTime(loginLog.getLoginTime()));
                activity.put("type", "success");
                activity.put("color", "#67C23A");
                activities.add(activity);
            }
            
            // 获取最近的操作日志
            LambdaQueryWrapper<SysOperationLog> operationQuery = new LambdaQueryWrapper<>();
            operationQuery.eq(SysOperationLog::getStatus, 1) // 只获取成功操作的记录
                    .orderByDesc(SysOperationLog::getCreatedAt)
                    .last("LIMIT " + (limit / 2)); // 操作日志占一半
            
            List<SysOperationLog> operationLogs = sysOperationLogMapper.selectList(operationQuery);
            
            for (SysOperationLog operationLog : operationLogs) {
                Map<String, Object> activity = new HashMap<>();
                String operationType = operationLog.getOperationType();
                String content = "用户 " + operationLog.getUsername() + " 执行了 " + operationType + " 操作";
                
                activity.put("content", content);
                activity.put("time", formatDateTime(operationLog.getCreatedAt()));
                
                // 根据操作类型设置不同的样式
                if (operationType != null) {
                    if (operationType.contains("新增") || operationType.contains("创建")) {
                        activity.put("type", "success");
                        activity.put("color", "#67C23A");
                    } else if (operationType.contains("删除")) {
                        activity.put("type", "danger");
                        activity.put("color", "#F56C6C");
                    } else if (operationType.contains("修改") || operationType.contains("更新")) {
                        activity.put("type", "warning");
                        activity.put("color", "#E6A23C");
                    } else {
                        activity.put("type", "primary");
                        activity.put("color", "#409EFF");
                    }
                } else {
                    activity.put("type", "info");
                    activity.put("color", "#909399");
                }
                
                activities.add(activity);
            }
            
            // 按时间倒序排序
            activities.sort((a, b) -> {
                String timeA = (String) a.get("time");
                String timeB = (String) b.get("time");
                return timeB.compareTo(timeA);
            });
            
            // 限制返回数量
            if (activities.size() > limit) {
                activities = activities.subList(0, limit);
            }
            
        } catch (Exception e) {
            log.error("获取系统动态数据失败", e);
            // 返回默认数据
            Map<String, Object> defaultActivity = new HashMap<>();
            defaultActivity.put("content", "系统运行正常");
            defaultActivity.put("time", formatDateTime(new Date()));
            defaultActivity.put("type", "info");
            defaultActivity.put("color", "#909399");
            activities.add(defaultActivity);
        }
        
        return activities;
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }
}
