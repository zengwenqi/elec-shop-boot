package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.mapper.purchase.PurchaseOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【purchase_order(采购订单主表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
    implements PurchaseOrderService{

}




