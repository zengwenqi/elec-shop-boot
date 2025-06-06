package elec.shop.service.purchase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.PurchaserInfo;
import elec.shop.pojo.purchase.dto.PurchaserQueryDTO;

/**
* @author Lenovo
* @description 针对表【purchaser_info(采购员信息表)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface PurchaserInfoService extends IService<PurchaserInfo> {
    /**
     * 查询采购员列表
     */
    Page<PurchaserInfo> queryPurchasers(PurchaserQueryDTO purchaserQueryDTO);

    /**
     * 新增采购员
     */
    PurchaserInfo addPurchaser(Long userId);

    /**
     * 删除采购员
     */
    void deletePurchaser(Long purchaserId);

    /**
     * 检查用户是否是采购员
     */
    boolean isPurchaser(Long userId);

    /**
     * 获取采购员信息
     */
    PurchaserInfo getPurchaserByUserId(Long userId);
}
