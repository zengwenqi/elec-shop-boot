package elec.shop.job;

import elec.shop.service.purchase.PurchaseOrderDraftService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PurchaseOrderDraftCleanTask {

    @Resource
    private PurchaseOrderDraftService draftService;

    /**
     * 每天凌晨2点执行清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredDrafts() {
        log.info("开始清理过期的采购订单暂存数据");
        try {
            draftService.cleanExpiredDrafts();
            log.info("清理过期的采购订单暂存数据完成");
        } catch (Exception e) {
            log.error("清理过期的采购订单暂存数据失败", e);
        }
    }
}
