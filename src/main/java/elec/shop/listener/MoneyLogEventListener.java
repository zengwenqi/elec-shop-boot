package elec.shop.listener;

import elec.shop.event.MoneyLogEvent;
import elec.shop.service.finance.AsyncMoneyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

/**
 * 财务日志事件监听器
 * 监听财务操作事件，异步记录日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MoneyLogEventListener {

    private final AsyncMoneyLogService asyncMoneyLogService;

    /**
     * 监听财务日志事件（事务提交后处理）
     * 确保业务事务成功后再记录日志
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("moneyLogExecutor")
    public void handleMoneyLogEvent(MoneyLogEvent event) {
        try {
            log.debug("接收到财务日志事件: {}, 用户: {}, 操作: {}, 金额: {}", 
                    event.getEventId(), event.getUserId(), event.getOperationType(), event.getAmount());

            // 异步记录日志
            CompletableFuture<Boolean> future = asyncMoneyLogService.recordLogAsync(event);
            
            // 可选：添加回调处理
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("财务日志记录失败: {}", event.getEventId(), throwable);
                } else if (result) {
                    log.debug("财务日志记录成功: {}", event.getEventId());
                } else {
                    log.warn("财务日志记录返回失败: {}", event.getEventId());
                }
            });

        } catch (Exception e) {
            log.error("处理财务日志事件失败: {}", event.getEventId(), e);
        }
    }

    /**
     * 监听财务日志事件（立即处理，不等待事务）
     * 用于一些不依赖事务的场景
     */
    @EventListener
    @Async("moneyLogExecutor")
    public void handleMoneyLogEventImmediate(MoneyLogEvent event) {
        // 检查是否需要立即处理（通过事件标记）
        if (event.getSource() != null && event.getSource().contains("IMMEDIATE")) {
            try {
                log.debug("立即处理财务日志事件: {}", event.getEventId());
                asyncMoneyLogService.recordLogAsync(event);
            } catch (Exception e) {
                log.error("立即处理财务日志事件失败: {}", event.getEventId(), e);
            }
        }
    }

    /**
     * 处理批量财务日志事件
     */
    @EventListener
    @Async("moneyLogExecutor")
    public void handleBatchMoneyLogEvent(MoneyLogEvent[] events) {
        if (events == null || events.length == 0) {
            return;
        }

        try {
            log.debug("接收到批量财务日志事件，数量: {}", events.length);

            for (MoneyLogEvent event : events) {
                asyncMoneyLogService.recordLogAsync(event);
            }

        } catch (Exception e) {
            log.error("处理批量财务日志事件失败", e);
        }
    }
}