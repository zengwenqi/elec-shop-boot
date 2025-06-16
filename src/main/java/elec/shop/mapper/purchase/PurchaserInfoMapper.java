package elec.shop.mapper.purchase;

import elec.shop.pojo.purchase.PurchaserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import elec.shop.pojo.sys.dto.PurchaseInfoVO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【purchaser_info(采购员信息表)】的数据库操作Mapper
* @createDate 2025-06-05 11:28:32
* @Entity elec.shop.pojo.purchase.PurchaserInfo
*/
public interface PurchaserInfoMapper extends BaseMapper<PurchaserInfo> {

    /**
     * 查询采购员信息列表
     * @return
     */
    List<PurchaseInfoVO> selectPurchaserInfoList();
}




