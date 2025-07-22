package elec.shop.service.purchase;

import elec.shop.pojo.purchase.PurchaseOrderDraft;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lenovo
* @description 针对表【purchase_order_draft(采购订单暂存表)】的数据库操作Service
* @createDate 2025-07-22 16:36:27
*/
public interface PurchaseOrderDraftService extends IService<PurchaseOrderDraft> {
    /**
     * 保存暂存数据
     */
    boolean saveDraft(PurchaseOrderDraft draft);

    /**
     * 获取用户的暂存数据
     */
    PurchaseOrderDraft getDraftByUserId(Long userId);

    /**
     * 删除用户的暂存数据
     */
    boolean deleteDraftByUserId(Long userId);

    /**
     * 清理过期数据
     */
    void cleanExpiredDrafts();
}
