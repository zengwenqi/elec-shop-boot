package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.PurchaseTask;
import elec.shop.service.purchase.PurchaseTaskService;
import elec.shop.mapper.purchase.PurchaseTaskMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【purchase_task(采购任务表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
public class PurchaseTaskServiceImpl extends ServiceImpl<PurchaseTaskMapper, PurchaseTask>
    implements PurchaseTaskService{

}




