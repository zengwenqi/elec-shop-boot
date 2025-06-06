package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.PurchaserInfo;
import elec.shop.pojo.purchase.dto.PurchaserQueryDTO;
import elec.shop.service.purchase.PurchaserInfoService;
import elec.shop.mapper.purchase.PurchaserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Lenovo
* @description 针对表【purchaser_info(采购员信息表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
public class PurchaserInfoServiceImpl extends ServiceImpl<PurchaserInfoMapper, PurchaserInfo>
    implements PurchaserInfoService{

    @Override
    public Page<PurchaserInfo> queryPurchasers(PurchaserQueryDTO purchaserQueryDTO) {
        LambdaQueryWrapper<PurchaserInfo> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(purchaserQueryDTO.getPurchaserCode())) {
            wrapper.eq(PurchaserInfo::getPurchaserCode, purchaserQueryDTO.getPurchaserCode());
        }

        wrapper.orderByDesc(PurchaserInfo::getCreatedAt);
        return page(new Page<>(purchaserQueryDTO.getPage(), purchaserQueryDTO.getSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaserInfo addPurchaser(Long userId) {
        // 检查是否已经是采购员
        if (isPurchaser(userId)) {
            throw new RuntimeException("该用户已经是采购员");
        }

        PurchaserInfo purchaserInfo = new PurchaserInfo();
        purchaserInfo.setUserId(userId);
        // 生成采购员编号 P + 用户ID后6位 + 4位随机数
        String purchaserCode = "P" + String.format("%06d", userId % 1000000)
                + String.format("%04d", (int)(Math.random() * 10000));
        purchaserInfo.setPurchaserCode(purchaserCode);

        save(purchaserInfo);
        return purchaserInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaser(Long purchaserId) {
        removeById(purchaserId);
    }

    @Override
    public boolean isPurchaser(Long userId) {
        return count(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getUserId, userId)) > 0;
    }

    @Override
    public PurchaserInfo getPurchaserByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<PurchaserInfo>()
                .eq(PurchaserInfo::getUserId, userId));
    }
}




