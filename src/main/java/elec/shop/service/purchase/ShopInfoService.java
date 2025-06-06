package elec.shop.service.purchase;

import elec.shop.pojo.purchase.ShopInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【shop_info(店铺信息表)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface ShopInfoService extends IService<ShopInfo> {

    /**
     * 查询我的店铺信息
     * @return
     */
    List<ShopInfo> queryShopInfo();

    /**
     * 测试
     * @param userId
     */
    void test(Long userId);
}
