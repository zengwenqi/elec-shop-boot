package elec.shop.mapper.purchase;

import elec.shop.pojo.purchase.PurchaseOrderDraft;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Lenovo
* @description 针对表【purchase_order_draft(采购订单暂存表)】的数据库操作Mapper
* @createDate 2025-07-22 16:36:27
* @Entity elec.shop.pojo.purchase.PurchaseOrderDraft
*/
public interface PurchaseOrderDraftMapper extends BaseMapper<PurchaseOrderDraft> {
    PurchaseOrderDraft selectDraftByUserId(@Param("userId") Long userId);
}




