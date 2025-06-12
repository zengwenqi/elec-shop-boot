package elec.shop.service.purchase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.pojo.purchase.dto.PurchaseOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaseOrderQueryDTO;
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

    /**
     * 查询用户订单列表
     */
    Page<PurchaseOrder> queryUserOrders(PurchaseOrderQueryDTO query);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, Integer orderStatus, String remark);

    /**
     * 取消订单
     */
    Boolean cancelOrder(Long orderId, String cancelReason);

    /**
     * 确认订单
     */
    void confirmOrder(Long orderId);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId);

    /**
     * 查询订单详情
     */
    PurchaseOrderDTO orderInfo(Long orderId);
}
