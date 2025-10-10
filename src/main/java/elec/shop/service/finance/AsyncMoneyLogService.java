package elec.shop.service.finance;

import elec.shop.event.MoneyLogEvent;
import elec.shop.pojo.finance.MoneyLog;
import elec.shop.service.finance.MoneyLogService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步财务日志服务
 * 实现批量入库和缓存机制，降低数据库压力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMoneyLogService {

    private final MoneyLogService moneyLogService;

    /**
     * 日志缓存队列
     */
    private final BlockingQueue<MoneyLogEvent> logQueue = new LinkedBlockingQueue<>(10000);

    /**
     * 批量处理大小
     */
    private static final int BATCH_SIZE = 50;

    /**
     * 批量处理间隔（毫秒）
     */
    private static final long BATCH_INTERVAL = 5000;

    /**
     * 处理线程池
     */
    private ScheduledExecutorService executorService;

    /**
     * 服务状态
     */
    private volatile boolean running = false;

    /**
     * 处理统计
     */
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);

    /**
     * 初始化服务
     */
    @PostConstruct
    public void init() {
        this.executorService = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "money-log-processor");
            thread.setDaemon(true);
            return thread;
        });

        this.running = true;

        // 启动批量处理任务
        executorService.scheduleWithFixedDelay(this::processBatch,
                BATCH_INTERVAL, BATCH_INTERVAL, TimeUnit.MILLISECONDS);

        // 启动统计任务
        executorService.scheduleWithFixedDelay(this::printStatistics,
                60000, 60000, TimeUnit.MILLISECONDS);

        log.info("异步财务日志服务已启动，批量大小: {}, 处理间隔: {}ms", BATCH_SIZE, BATCH_INTERVAL);
    }

    /**
     * 销毁服务
     */
    @PreDestroy
    public void destroy() {
        this.running = false;

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 处理剩余的日志
        processRemainingLogs();

        log.info("异步财务日志服务已关闭，总处理: {}, 失败: {}",
                processedCount.get(), failedCount.get());
    }

    /**
     * 异步记录财务日志
     */
    @Async("moneyLogExecutor")
    public CompletableFuture<Boolean> recordLogAsync(MoneyLogEvent event) {
        if (!running) {
            log.warn("异步财务日志服务未运行，直接同步处理");
            return CompletableFuture.completedFuture(recordLogSync(event));
        }

        try {
            // 设置事件时间
            if (event.getEventTime() == null) {
                event.setEventTime(LocalDateTime.now());
            }

            // 添加到队列
            boolean added = logQueue.offer(event, 1, TimeUnit.SECONDS);
            if (!added) {
                log.warn("财务日志队列已满，直接同步处理: {}", event.getEventId());
                return CompletableFuture.completedFuture(recordLogSync(event));
            }

            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("异步记录财务日志失败: {}", event.getEventId(), e);
            // 异步失败时，尝试同步处理
            return CompletableFuture.completedFuture(recordLogSync(event));
        }
    }

    /**
     * 同步记录财务日志（兜底方案）
     */
    public boolean recordLogSync(MoneyLogEvent event) {
        try {
            MoneyLog moneyLog = convertEventToLog(event);
            moneyLogService.save(moneyLog);
            processedCount.incrementAndGet();
            return true;
        } catch (Exception e) {
            log.error("同步记录财务日志失败: {}", event.getEventId(), e);
            failedCount.incrementAndGet();
            return false;
        }
    }

    /**
     * 批量处理日志
     */
    private void processBatch() {
        if (!running || logQueue.isEmpty()) {
            return;
        }

        List<MoneyLogEvent> batch = new ArrayList<>();

        try {
            // 收集批量数据
            logQueue.drainTo(batch, BATCH_SIZE);

            if (batch.isEmpty()) {
                return;
            }

            log.debug("开始批量处理财务日志，数量: {}", batch.size());

            // 转换为MoneyLog对象
            List<MoneyLog> moneyLogs = new ArrayList<>();
            for (MoneyLogEvent event : batch) {
                try {
                    MoneyLog moneyLog = convertEventToLog(event);
                    moneyLogs.add(moneyLog);
                } catch (Exception e) {
                    log.error("转换财务日志事件失败: {}", event.getEventId(), e);
                    failedCount.incrementAndGet();
                }
            }

            // 批量保存
            if (!moneyLogs.isEmpty()) {
                moneyLogService.saveBatch(moneyLogs);
                processedCount.addAndGet(moneyLogs.size());
                log.debug("批量处理财务日志完成，成功: {}", moneyLogs.size());
            }

        } catch (Exception e) {
            log.error("批量处理财务日志失败", e);
            failedCount.addAndGet(batch.size());

            // 失败时尝试逐个处理
            for (MoneyLogEvent event : batch) {
                try {
                    recordLogSync(event);
                } catch (Exception ex) {
                    log.error("单个处理财务日志失败: {}", event.getEventId(), ex);
                }
            }
        }
    }

    /**
     * 处理剩余日志
     */
    private void processRemainingLogs() {
        List<MoneyLogEvent> remaining = new ArrayList<>();
        logQueue.drainTo(remaining);

        if (!remaining.isEmpty()) {
            log.info("处理剩余财务日志: {}", remaining.size());
            for (MoneyLogEvent event : remaining) {
                recordLogSync(event);
            }
        }
    }

    /**
     * 转换事件为日志对象
     */
    private MoneyLog convertEventToLog(MoneyLogEvent event) {
        MoneyLog moneyLog = new MoneyLog();
        BeanUtils.copyProperties(event, moneyLog);

        // 设置创建时间
        if (moneyLog.getCreatedAt() == null) {
            moneyLog.setCreatedAt(event.getBusinessTime() != null ?
                    event.getBusinessTime() : LocalDateTime.now());
        }

        // 设置更新时间
        moneyLog.setUpdatedAt(LocalDateTime.now());

        return moneyLog;
    }

    /**
     * 打印统计信息
     */
    private void printStatistics() {
        int queueSize = logQueue.size();
        int processed = processedCount.get();
        int failed = failedCount.get();

        if (processed > 0 || failed > 0 || queueSize > 0) {
            log.info("财务日志处理统计 - 队列: {}, 已处理: {}, 失败: {}",
                    queueSize, processed, failed);
        }
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return logQueue.size();
    }

    /**
     * 获取处理统计
     */
    public String getStatistics() {
        return String.format("队列: %d, 已处理: %d, 失败: %d",
                logQueue.size(), processedCount.get(), failedCount.get());
    }

    /**
     * 强制处理所有队列中的日志
     */
    public void flushAll() {
        log.info("强制处理所有队列中的财务日志");
        while (!logQueue.isEmpty()) {
            processBatch();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
