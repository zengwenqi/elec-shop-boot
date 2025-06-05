package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.PurchaseOrderLog;
import elec.shop.service.purchase.PurchaseOrderLogService;
import elec.shop.mapper.purchase.PurchaseOrderLogMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【purchase_order_log(采购订单日志表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
public class PurchaseOrderLogServiceImpl extends ServiceImpl<PurchaseOrderLogMapper, PurchaseOrderLog>
    implements PurchaseOrderLogService{

}




