package elec.shop.mapper.purchase;

import elec.shop.pojo.purchase.PurchaseOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Lenovo
* @description 针对表【purchase_order_item(采购订单商品明细表)】的数据库操作Mapper
* @createDate 2025-06-13 15:58:23
* @Entity elec.shop.pojo.purchase.PurchaseOrderItem
*/
@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {

}




