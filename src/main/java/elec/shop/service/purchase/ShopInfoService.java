package elec.shop.service.purchase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.purchase.ShopInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.dto.ShopInfoDTO;
import elec.shop.pojo.purchase.dto.ShopInfoQueryDTO;
import elec.shop.utils.Result;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取店铺订单统计信息
     * @param shopId 店铺ID
     * @return 统计信息
     */
    Map<String, Object> getShopOrderStatistics(Long shopId);

    /**
     * 获取所有店铺信息
     * @return 店铺信息列表
     */
    List<ShopInfo> queryShopInfoList();

    /**
     * 初始化用户基本数据
     * @param userId 用户ID
     */
    void initUserInfoData(Long userId,String username);
}
