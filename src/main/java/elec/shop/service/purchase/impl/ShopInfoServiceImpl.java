package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.pojo.purchase.ShopInfo;
import elec.shop.service.purchase.ShopInfoService;
import elec.shop.pojo.sys.SysUser;
import elec.shop.security.CustomUserDetails;
import elec.shop.mapper.purchase.ShopInfoMapper;
import elec.shop.utils.AllContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


/**
* @author Lenovo
* @description 针对表【shop_info(店铺信息表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
public class ShopInfoServiceImpl extends ServiceImpl<ShopInfoMapper, ShopInfo>
    implements ShopInfoService {

    private final ShopInfoMapper shopInfoMapper;
    private final AccountBalanceMapper accountBalanceMapper;
    private final FinanceAccountMapper financeAccountMapper;

    @Override
    public List<ShopInfo> queryShopInfo() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        return shopInfoMapper.selectList(new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getUserId,loginSysUser.getUserId()));
    }


    /**
     * 初始化用户信息数据
     */
    public void initUserInfoData(Long userId) {
        // 初始化店铺数据
        ShopInfo shopInfo = new ShopInfo();
        String s = AllContextUtils.generateUniqueShopNumber(userId);
        shopInfo.setUserId(userId);
        shopInfo.setShopCode(s);
        shopInfo.setShopName("默认店铺");
        shopInfo.setShopType(1);
        shopInfo.setShopLogo("默认logo");
        shopInfo.setShopDesc("初始化用户携带的默认店铺");

    }
}




