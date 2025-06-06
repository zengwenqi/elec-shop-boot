package elec.shop.service.purchase;

import elec.shop.pojo.purchase.PurchaseOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.dto.PurchaseOrderDTO;
import elec.shop.utils.Result;

/**
* @author Lenovo
* @description 针对表【purchase_order(采购订单主表)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 创建采购订单
     * @param purchaseOrderDTO
     * @return
     */
    Result createOrder(PurchaseOrderDTO purchaseOrderDTO);
}
