package elec.shop.service.purchase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.pojo.purchase.dto.PurchaserOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaserOrderQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaserOrderVO;
import elec.shop.utils.Result;
import jakarta.servlet.http.HttpServletResponse;

/**
* @author Lenovo
* @description 针对表【purchase_order(采购订单主表)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 创建采购订单
     * @param purchaserOrderDTO
     * @return
     */
    Result createOrder(PurchaserOrderDTO purchaserOrderDTO);

    /**
     * 查询用户订单列表
     */
    IPage<PurchaserOrderVO> queryUserOrders(PurchaserOrderQueryDTO query);

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
    PurchaserOrderVO orderInfo(Long orderId);

    /**
     * 导出订单数据
     * @param shopId 店铺ID，可选
     * @param startTime 开始时间，可选
     * @param endTime 结束时间，可选
     * @param response HTTP响应对象
     */
    void exportOrders(Long shopId, String startTime, String endTime, HttpServletResponse response);
}
