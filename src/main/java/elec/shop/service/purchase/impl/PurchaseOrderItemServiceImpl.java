package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.PurchaseOrderItem;
import elec.shop.service.purchase.PurchaseOrderItemService;
import elec.shop.mapper.purchase.PurchaseOrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【purchase_order_item(采购订单商品明细表)】的数据库操作Service实现
* @createDate 2025-06-13 15:58:23
*/
@Service
public class PurchaseOrderItemServiceImpl extends ServiceImpl<PurchaseOrderItemMapper, PurchaseOrderItem>
    implements PurchaseOrderItemService{

}




