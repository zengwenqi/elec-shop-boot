package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.mapper.purchase.PurchaseOrderMapper;
import elec.shop.mapper.purchase.ShopInfoMapper;
import elec.shop.pojo.purchase.*;
import elec.shop.pojo.purchase.dto.PurchaserOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaserOrderQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaserOrderExportVO;
import elec.shop.pojo.purchase.vo.PurchaserOrderItemVO;
import elec.shop.pojo.purchase.vo.PurchaserOrderVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.PurchaseOrderItemService;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.service.purchase.PurchaserInfoService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.ExcelUtils;
import elec.shop.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【purchase_order(采购订单主表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
    implements PurchaseOrderService{

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final AccountBalanceMapper accountBalanceMapper;
    private final FinanceAccountMapper financeAccountMapper;
    private final ShopInfoMapper shopInfoMapper;
    private final PurchaseOrderItemService purchaseOrderItemService;
    private final PurchaserInfoService purchaserInfoService;
    private final SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createOrder(PurchaserOrderDTO purchaserOrderDTO) {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        // 查询当前用户的财务账户余额和汇率
        FinanceAccount financeAccount = financeAccountMapper.selectOne(new LambdaQueryWrapper<FinanceAccount>()
                .eq(FinanceAccount::getUserId, loginSysUser.getUserId()));
        AccountBalance accountBalance = accountBalanceMapper.selectOne(new LambdaQueryWrapper<AccountBalance>()
                .eq(AccountBalance::getAccountId, financeAccount.getAccountId())
                .eq(AccountBalance::getCurrency, purchaserOrderDTO.getCurrency()));
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        BeanUtils.copyProperties(purchaserOrderDTO,purchaseOrder);
        //  生成订单编号
        purchaseOrder.setOrderNo(AllContextUtils.generateOrderNumber(loginSysUser.getUserId()));
        // 订单状态、支付状态、汇率、确认时间
        // 默认待确定状态(采购员确认)
        purchaseOrder.setOrderStatus(0);
        // 支付状态：待支付(系统设置默认支付)
        purchaseOrder.setPaymentStatus(1);
        purchaseOrder.setExchangeRate(accountBalance.getExchangeRate());
        purchaseOrder.setConfirmTime(new Date());
        purchaseOrder.setUserId(loginSysUser.getUserId());

        List<PurchaseOrderItem> purchaseOrderItemList = new ArrayList<>();
        int insert = purchaseOrderMapper.insert(purchaseOrder);
        purchaserOrderDTO.getOrderItems().forEach(e-> {
            PurchaseOrderItem purchaseOrderItem = new PurchaseOrderItem();
            BeanUtils.copyProperties(e,purchaseOrderItem);
            purchaseOrderItem.setOrderId(purchaseOrder.getOrderId());
            purchaseOrderItemList.add(purchaseOrderItem);
        });
        boolean b = purchaseOrderItemService.saveBatch(purchaseOrderItemList);
        if (insert <= 0&&b)
            return Result.fail().message("采购订单创建失败");
        return Result.ok();
    }

    @Override
    public IPage<PurchaserOrderVO> queryUserOrders(PurchaserOrderQueryDTO query) {
        // 1. 构建主表查询条件
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getIsDeleted, 0);

        if (query.getShopId() != null) {
            wrapper.eq(PurchaseOrder::getShopId, query.getShopId());
        }
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
            wrapper.le(PurchaseOrder::getUpdatedAt, query.getEndTime());
        }
        wrapper.last("ORDER BY CASE WHEN order_status = 5 THEN 1 ELSE 0 END, created_at DESC");
        // 2. 主表分页查询
        Page<PurchaseOrder> page = new Page<>(query.getPage(), query.getSize());
        IPage<PurchaseOrder> orderPage = this.page(page, wrapper);

        // 3. 获取分页后的订单ID列表
        List<Long> orderIds = orderPage.getRecords().stream()
                .map(PurchaseOrder::getOrderId)
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return new Page<>(query.getPage(), query.getSize());
        }

        // 4. 查询订单项数据
        List<PurchaseOrderItem> orderItems = purchaseOrderItemService.list(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .in(PurchaseOrderItem::getOrderId, orderIds)
        );

        // 5. 组装数据
        List<PurchaserOrderVO> orderVOs = orderPage.getRecords().stream().map(order -> {
            PurchaserOrderVO vo = new PurchaserOrderVO();
            BeanUtils.copyProperties(order, vo);
            if (order.getPurchaserId() != null)
                vo.setPurchaserName(sysUserService
                    .getById(purchaserInfoService
                            .getById(order.getPurchaserId())
                            .getUserId())
                    .getRealName());
            // 设置订单项
            List<PurchaserOrderItemVO> itemVOs = orderItems.stream()
                .filter(item -> item.getOrderId().equals(order.getOrderId()))
                .map(item -> {
                    PurchaserOrderItemVO itemVO = new PurchaserOrderItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    return itemVO;
                })
                .collect(Collectors.toList());
            vo.setOrderItems(itemVOs);

            return vo;
        }).collect(Collectors.toList());

        // 6. 组装分页结果
        Page<PurchaserOrderVO> resultPage = new Page<>(query.getPage(), query.getSize(), orderPage.getTotal());
        resultPage.setRecords(orderVOs);

        return resultPage;
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
    public Boolean cancelOrder(Long orderId, String cancelReason) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() >= 2) { // 采购中或之后的状态不能取消
            throw new RuntimeException("当前订单状态不能取消");
        }

        order.setOrderStatus(5); // 已取消
        order.setCancelReason(cancelReason);
        order.setCancelTime(new Date());
        return this.updateById(order);
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

    @Override
    public PurchaserOrderVO orderInfo(Long orderId) {
        PurchaserOrderVO purchaserOrderVO = purchaseOrderMapper.queryPurchaseOrderOne(orderId);
        return purchaserOrderVO;
    }

    @Override
    public void exportOrders(Long shopId, String startTime, String endTime, HttpServletResponse response) {
        // 获取当前登录用户
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();

        // 如果指定了店铺ID，验证店铺是否属于当前用户
        if (shopId != null) {
            ShopInfo shopInfo = shopInfoMapper.selectOne(new LambdaQueryWrapper<ShopInfo>()
                    .eq(ShopInfo::getShopId, shopId)
                    .eq(ShopInfo::getUserId, loginSysUser.getUserId()));

            if (shopInfo == null) {
                throw new RuntimeException("无权访问该店铺信息");
            }
        }

        // 查询导出数据
        List<PurchaserOrderExportVO> exportData = purchaseOrderMapper.selectExportOrders(
                loginSysUser.getUserId(), shopId, startTime, endTime);

        // 导出Excel（使用支持合并单元格的方法）
        String fileName = "采购订单数据";
        String sheetName = "订单列表";
        ExcelUtils.exportExcelWithMerge(response, exportData, fileName, sheetName, PurchaserOrderExportVO.class);
    }
}




