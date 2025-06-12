package elec.shop.service.purchase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.purchase.ShopInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.dto.ShopInfoDTO;
import elec.shop.pojo.purchase.dto.ShopInfoQueryDTO;
import elec.shop.utils.Result;

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
    IPage<ShopInfo> queryShopInfo(ShopInfoQueryDTO shopInfoQueryDTO);

    /**
     * 测试
     * @param userId
     */
    void test(Long userId);

    /**
     * 更新我的店铺信息
     * @param shopInfoDTO
     * @return
     */
    Result updateShopInfo(ShopInfoDTO shopInfoDTO);

    /**
     * 删除店铺
     * @param shopId
     * @return
     */
    Result deleteShop(Long shopId);

    /**
     * 新增店铺
     * @param shopInfoDTO
     * @return
     */
    Result addShop(ShopInfoDTO shopInfoDTO);
}
