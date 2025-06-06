package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.mapper.purchase.PurchaseOrderMapper;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.pojo.purchase.PurchaseOrder;
import elec.shop.pojo.purchase.dto.PurchaseOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaseOrderQueryDTO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
* @author Lenovo
* @description 针对表【purchase_order(采购订单主表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
    implements PurchaseOrderService{

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final AccountBalanceMapper accountBalanceMapper;
    private final FinanceAccountMapper financeAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createOrder(PurchaseOrderDTO purchaseOrderDTO) {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        // 查询当前用户的财务账户余额和汇率
        FinanceAccount financeAccount = financeAccountMapper.selectOne(new LambdaQueryWrapper<FinanceAccount>()
                .eq(FinanceAccount::getUserId, loginSysUser.getUserId()));
        AccountBalance accountBalance = accountBalanceMapper.selectOne(new LambdaQueryWrapper<AccountBalance>()
                .eq(AccountBalance::getAccountId, financeAccount.getAccountId())
                .eq(AccountBalance::getCurrency, purchaseOrderDTO.getCurrency()));
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        BeanUtils.copyProperties(purchaseOrderDTO,purchaseOrder);
        //  生成订单编号
        purchaseOrder.setOrderNo(AllContextUtils.generateOrderNumber(loginSysUser.getUserId()));
        // 订单状态、支付状态、汇率、确认时间
        // 默认待确定状态(采购员确认)
        purchaseOrder.setOrderStatus(0);
        // 支付状态：待支付(系统设置默认支付)
        purchaseOrder.setPaymentStatus(1);
        purchaseOrder.setExchangeRate(accountBalance.getExchangeRate());
        purchaseOrder.setConfirmTime(new Date());

        int insert = purchaseOrderMapper.insert(purchaseOrder);
        if (insert <= 0)
            return Result.fail().message("采购订单创建失败");
        return Result.ok();
    }

    @Override
    public Page<PurchaseOrder> queryUserOrders(PurchaseOrderQueryDTO query) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getUserId, AllContextUtils.getLoginSysUser().getUserId());

        // 添加查询条件
        if (query.getOrderStatus() != null) {
            wrapper.eq(PurchaseOrder::getOrderStatus, query.getOrderStatus());
        }
        if (query.getPaymentStatus() != null) {
            wrapper.eq(PurchaseOrder::getPaymentStatus, query.getPaymentStatus());
        }
        if (StringUtils.isNotBlank(query.getStartTime())) {
            wrapper.ge(PurchaseOrder::getCreatedAt, query.getStartTime());
        }
        if (StringUtils.isNotBlank(query.getEndTime())) {
            wrapper.le(PurchaseOrder::getCreatedAt, query.getEndTime());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(PurchaseOrder::getCreatedAt);

        return page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, Integer orderStatus, String remark) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        order.setOrderStatus(orderStatus);
        order.setRemark(remark);
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String cancelReason) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() >= 2) { // 采购中或之后的状态不能取消
            throw new RuntimeException("当前订单状态不能取消");
        }

        order.setOrderStatus(4); // 已取消
        order.setCancelReason(cancelReason);
        order.setCancelTime(new Date());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long orderId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("只有待确认的订单可以确认");
        }

        order.setOrderStatus(1); // 已确认
        order.setConfirmTime(new Date());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        PurchaseOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("只有采购中的订单可以完成");
        }

        order.setOrderStatus(3); // 已完成
        order.setCompleteTime(new Date());
        updateById(order);
    }
}




