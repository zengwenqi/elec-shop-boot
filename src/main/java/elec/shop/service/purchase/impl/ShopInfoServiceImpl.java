package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.mapper.purchase.PurchaseOrderMapper;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.pojo.purchase.ShopInfo;
import elec.shop.pojo.purchase.dto.ShopInfoDTO;
import elec.shop.pojo.purchase.dto.ShopInfoQueryDTO;
import elec.shop.sms.ExchangeRateService;
import elec.shop.service.purchase.ShopInfoService;
import elec.shop.pojo.sys.SysUser;
import elec.shop.mapper.purchase.ShopInfoMapper;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


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
    private final ExchangeRateService exchangeRateService;
    private final PurchaseOrderMapper  purchaseOrderMapper;

    @Override
    public IPage<ShopInfo> queryShopInfo(ShopInfoQueryDTO shopInfoQueryDTO) {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        LambdaQueryWrapper<ShopInfo> queryWrapper = new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getUserId, loginSysUser.getUserId());
        if (shopInfoQueryDTO.getShopName()!=null&&!(shopInfoQueryDTO.getShopName().isEmpty())) {
            queryWrapper.eq(ShopInfo::getShopName, shopInfoQueryDTO.getShopName());
        }
        if (shopInfoQueryDTO.getShopCode()!=null) {
            queryWrapper.eq(ShopInfo::getShopCode, shopInfoQueryDTO.getShopCode());
        }
        if (shopInfoQueryDTO.getShopType()!=null) {
            queryWrapper.eq(ShopInfo::getShopType, shopInfoQueryDTO.getShopType());
        }
        IPage page = new Page(shopInfoQueryDTO.getPageNum(), shopInfoQueryDTO.getPageSize());
        return shopInfoMapper.selectPage(page,queryWrapper);
    }

    @Override
    @Transactional
    public void test(Long userId) {
        initUserInfoData(userId);
    }

    @Override
    @Transactional
    public Result updateShopInfo(ShopInfoDTO shopInfoDTO) {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        ShopInfo shopInfo = shopInfoMapper.selectOne(new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getShopId, shopInfoDTO.getShopId()));
        if (loginSysUser.getUserId().equals(shopInfo.getUserId())){
            ShopInfo shopInfo1 = new ShopInfo();
            BeanUtils.copyProperties(shopInfoDTO, shopInfo1);
            shopInfoMapper.updateById(shopInfo1);
            return Result.ok();
        }
        return Result.fail().message("无权操作");
    }

    @Override
    @Transactional
    public Result deleteShop(Long shopId) {
        ShopInfo shopInfo = shopInfoMapper.selectOne(new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getShopId, shopId));
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        if (shopInfo.getUserId().equals(loginSysUser.getUserId())){
            Long l = purchaseOrderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
                    .eq(PurchaseOrder::getShopId, shopId));
            if (l > 0){
                return Result.fail().message("该店铺下有订单，请先处理订单");
            }
            shopInfoMapper.deleteById(shopId);
            return Result.ok();
        }
        return Result.fail().message("无权操作");
    }

    @Override
    @Transactional
    public Result addShop(ShopInfoDTO shopInfoDTO) {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        ShopInfo shopInfo = new ShopInfo();
        BeanUtils.copyProperties(shopInfoDTO, shopInfo);
        shopInfo.setUserId(loginSysUser.getUserId());
        shopInfo.setShopCode(AllContextUtils.generateUniqueShopNumber(loginSysUser.getUserId()));
        int insert = shopInfoMapper.insert(shopInfo);
        if (insert > 0) return Result.ok();
        return Result.fail().message("无权操作");
    }

    @Override
    public Map<String, Object> getShopOrderStatistics(Long shopId) {
        // 验证店铺是否属于当前用户
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        ShopInfo shopInfo = shopInfoMapper.selectOne(new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getShopId, shopId)
                .eq(ShopInfo::getUserId, loginSysUser.getUserId()));

        if (shopInfo == null) {
            throw new RuntimeException("无权访问该店铺信息");
        }

        Map<String, Object> statistics = new HashMap<>();

        // 统计各状态订单数量
        LambdaQueryWrapper<PurchaseOrder> orderWrapper = new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getShopId, shopId);

        // 总订单数
        long totalOrders = purchaseOrderMapper.selectCount(orderWrapper);
        statistics.put("totalOrders", totalOrders);

        // 待确认订单数
        long pendingConfirmOrders = purchaseOrderMapper.selectCount(
                orderWrapper.clone().eq(PurchaseOrder::getOrderStatus, 0));
        statistics.put("pendingConfirmOrders", pendingConfirmOrders);

        // 采购中订单数
        long processingOrders = purchaseOrderMapper.selectCount(
                orderWrapper.clone().eq(PurchaseOrder::getOrderStatus, 2));
        statistics.put("processingOrders", processingOrders);

        // 已完成订单数
        long completedOrders = purchaseOrderMapper.selectCount(
                orderWrapper.clone().eq(PurchaseOrder::getOrderStatus, 3));
        statistics.put("completedOrders", completedOrders);

        // 已取消订单数
        long cancelledOrders = purchaseOrderMapper.selectCount(
                orderWrapper.clone().eq(PurchaseOrder::getOrderStatus, 4));
        statistics.put("cancelledOrders", cancelledOrders);

        // 统计订单总金额
        List<PurchaseOrder> allOrders = purchaseOrderMapper.selectList(orderWrapper);
        BigDecimal totalAmount = allOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("totalAmount", totalAmount);

        // 按币种统计订单金额
        Map<String, BigDecimal> amountByCurrency = allOrders.stream()
                .collect(Collectors.groupingBy(
                        PurchaseOrder::getCurrency,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                PurchaseOrder::getTotalAmount,
                                BigDecimal::add)));
        statistics.put("amountByCurrency", amountByCurrency);

        return statistics;
    }

    @Override
    public List<ShopInfo> queryShopInfoList() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        LambdaQueryWrapper<ShopInfo> queryWrapper = new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getUserId, loginSysUser.getUserId());
        return shopInfoMapper.selectList(queryWrapper);
    }

    /**
     * 初始化用户信息数据
     */
    @Override
    public void initUserInfoData(Long userId) {
        // 初始化店铺数据
        ShopInfo shopInfo = new ShopInfo();
        shopInfo.setUserId(userId);
        shopInfo.setShopCode(AllContextUtils.generateUniqueShopNumber(userId));
        shopInfo.setShopName("默认店铺");
        shopInfo.setShopType(1);
        shopInfo.setShopLogo("默认logo");
        shopInfo.setShopDesc("初始化用户携带的默认店铺");
        shopInfoMapper.insert(shopInfo);

        FinanceAccount financeAccount = new FinanceAccount();
        financeAccount.setAccountId(AllContextUtils.generateAccountId(userId));
        financeAccount.setUserId(userId);
        financeAccount.setAccountType(1);
        financeAccount.setAccountNo(AllContextUtils.generateAccountNo(userId));
        financeAccount.setBanlance(new BigDecimal("00.00"));
        financeAccount.setBaseCurrency("人民币");
        financeAccount.setStatus(1);
        financeAccountMapper.insert(financeAccount);

        // 创建账户余额对象列表
        AccountBalance usdBalance = createAccountBalance(financeAccount.getAccountId(), "USD", "$", "美元", new BigDecimal("00.00"));
        AccountBalance eurBalance = createAccountBalance(financeAccount.getAccountId(), "EUR", "€", "欧元", new BigDecimal("00.00"));
        AccountBalance gbpBalance = createAccountBalance(financeAccount.getAccountId(), "GBP", "£", "英镑", new BigDecimal("00.00"));
        AccountBalance jpyBalance = createAccountBalance(financeAccount.getAccountId(), "JPY", "¥", "日元", new BigDecimal("00.00"));

        // 保存账户余额信息
        accountBalanceMapper.insert(usdBalance);
        accountBalanceMapper.insert(eurBalance);
        accountBalanceMapper.insert(gbpBalance);
        accountBalanceMapper.insert(jpyBalance);
    }

    /**
     * 创建账户余额信息
     */
    private AccountBalance createAccountBalance(String accountId, String currency, String symbol, String currencyName, BigDecimal amount) {
        AccountBalance balance = new AccountBalance();
        // 设置账户ID
        balance.setAccountId(accountId);
        // 设置货币信息
        balance.setCurrency(currency);
        balance.setSymbol(symbol);
        balance.setCurrencyName(currencyName);
        // 设置金额信息
        balance.setBalance(amount);
        // 从汇率服务获取最新汇率
        balance.setExchangeRate(exchangeRateService.getExchangeRate(currency));
        // 设置最后更新时间
        balance.setLastUpdated(new Date());
        return balance;
    }
}




