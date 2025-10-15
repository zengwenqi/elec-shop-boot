package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.mapper.purchase.PurchaseOrderMapper;
import elec.shop.mapper.purchase.ShopInfoMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.purchase.*;
import elec.shop.pojo.purchase.dto.PurchaserOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaserOrderQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaserOrderExportVO;
import elec.shop.pojo.purchase.vo.PurchaserOrderItemVO;
import elec.shop.pojo.purchase.vo.PurchaserOrderVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.pojo.sys.vo.UserDetailVO;
import elec.shop.service.purchase.PurchaseOrderItemService;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.service.purchase.PurchaserInfoService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Collections;

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
    private final MinioUtil minioUtil;
    private final SysUserMapper sysUserMapper;

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
//        if (accountBalance.getBalance().compareTo(purchaseOrder.getTotalAmount().add(purchaseOrder.getServiceCharge())) < 0){
        if (accountBalance.getBalance().compareTo(purchaseOrder.getTotalAmount()) < 0){
            return Result.fail().message("余额不足");
        }
        BigDecimal balance = accountBalance.getBalance();
//        accountBalance.setBalance(accountBalance.getBalance().subtract(purchaseOrder.getTotalAmount().add(purchaseOrder.getServiceCharge())));
        accountBalance.setBalance(accountBalance.getBalance().subtract(purchaseOrder.getTotalAmount()));
        accountBalanceMapper.updateById(accountBalance);
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
            purchaseOrderItem.setProductImage(e.getFileName());
            purchaseOrderItem.setOrderId(purchaseOrder.getOrderId());
            purchaseOrderItemList.add(purchaseOrderItem);
        });
        boolean b = purchaseOrderItemService.saveBatch(purchaseOrderItemList);
        MoneyLogHelper.zidingyiLogRecharge(loginSysUser.getUserId(), loginSysUser.getUsername(), 4,
                "采购订单支付",purchaseOrder.getTotalAmount(),balance,balance.subtract(purchaseOrder.getTotalAmount()),
                purchaseOrder.getOrderNo(), "支付采购订单支付账单",
                AllContextUtils.getLoginSysUser().getUsername(), "系统", accountBalance.getCurrency());
        if (insert <= 0&&b)
            return Result.fail().message("采购订单创建失败");
        return Result.ok();
    }

    @Override
    public IPage<PurchaserOrderVO> queryUserOrders(PurchaserOrderQueryDTO query) {
        try {
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();
            SysUser byId = sysUserService.getById(loginSysUser.getUserId());
            // 1. 构建主表查询条件，使用索引优化
            LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>()
                    .eq(PurchaseOrder::getIsDeleted, 0)
                    .eq(query.getShopId() != null, PurchaseOrder::getShopId, query.getShopId())
                    .eq(query.getOrderStatus() != null, PurchaseOrder::getOrderStatus, query.getOrderStatus())
                    .ge(StringUtils.isNotBlank(query.getStartTime()), PurchaseOrder::getCreatedAt, query.getStartTime())
                    .le(StringUtils.isNotBlank(query.getEndTime()), PurchaseOrder::getUpdatedAt, query.getEndTime())
                    .like(StringUtils.isNotBlank(query.getOrderNo()), PurchaseOrder::getOrderNo, query.getOrderNo())
                    .like(StringUtils.isNotBlank(query.getGoodsOrderNo()), PurchaseOrder::getGoodsOrderNo, query.getGoodsOrderNo())
                    .orderByDesc(PurchaseOrder::getCreatedAt);
            if (byId.getUserType()==3||byId.getUserType()==4) {
                wrapper.eq(loginSysUser.getUserId() != null, PurchaseOrder::getUserId, loginSysUser.getUserId());
            }

            // 2. 主表分页查询
            Page<PurchaseOrder> page = new Page<>(query.getPage(), query.getSize());
            IPage<PurchaseOrder> orderPage = this.page(page, wrapper);

            if (orderPage.getRecords().isEmpty()) {
                return new Page<>(query.getPage(), query.getSize());
            }

            // 3. 获取分页后的订单ID列表
            List<Long> orderIds = orderPage.getRecords().stream()
                    .map(PurchaseOrder::getOrderId)
                    .collect(Collectors.toList());

            // 4. 并行查询订单项数据和采购员信息
            CompletableFuture<List<PurchaseOrderItem>> orderItemsFuture = CompletableFuture.supplyAsync(() ->
                purchaseOrderItemService.list(new LambdaQueryWrapper<PurchaseOrderItem>()
                    .in(PurchaseOrderItem::getOrderId, orderIds)));

            // 5. 使用Map存储采购员信息，避免重复查询
            Map<Long, String> purchaserNameCache = new ConcurrentHashMap<>();

            // 6. 并行处理订单数据
            List<PurchaserOrderVO> orderVOs = orderPage.getRecords().parallelStream().map(order -> {
                PurchaserOrderVO vo = new PurchaserOrderVO();
                BeanUtils.copyProperties(order, vo);

                // 设置采购员名称
                if (order.getPurchaserId() != null) {
                    vo.setPurchaserName(purchaserNameCache.computeIfAbsent(order.getPurchaserId(), purchaserId -> {
                        PurchaserInfo purchaserInfo = purchaserInfoService.getById(purchaserId);
                        if (purchaserInfo != null) {
                            SysUser user = sysUserService.getById(purchaserInfo.getUserId());
                            return user != null ? user.getRealName() : "未知";
                        }
                        return "未知";
                    }));
                }
                return vo;
            }).collect(Collectors.toList());

            // 7. 等待订单项数据查询完成并设置
            List<PurchaseOrderItem> orderItems = orderItemsFuture.get();
            Map<Long, List<PurchaseOrderItem>> itemMap = orderItems.stream()
                    .collect(Collectors.groupingBy(PurchaseOrderItem::getOrderId));

            // 8. 并行处理订单项数据
            orderVOs.parallelStream().forEach(vo -> {
                List<PurchaseOrderItem> items = itemMap.getOrDefault(vo.getOrderId(), Collections.emptyList());
                List<PurchaserOrderItemVO> itemVOs = items.parallelStream().map(item -> {
                    PurchaserOrderItemVO itemVO = new PurchaserOrderItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    itemVO.setProductImage(minioUtil.getPreviewUrl(item.getProductImage()));
                    return itemVO;
                }).collect(Collectors.toList());
                vo.setOrderItems(itemVOs);
            });

            // 9. 组装分页结果
            Page<PurchaserOrderVO> resultPage = new Page<>(query.getPage(), query.getSize(), orderPage.getTotal());
            resultPage.setRecords(orderVOs);

            return resultPage;
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            throw new BusinessException("查询订单列表失败: " + e.getMessage());
        }
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
        boolean b = this.updateById(order);
        if (b) {
            FinanceAccount financeAccount = financeAccountMapper.selectOne(new LambdaQueryWrapper<FinanceAccount>()
                    .eq(FinanceAccount::getUserId, order.getUserId()));
            AccountBalance accountBalance = accountBalanceMapper.selectOne(new LambdaQueryWrapper<AccountBalance>()
                    .eq(AccountBalance::getAccountId, financeAccount.getAccountId()));

            MoneyLogHelper.zidingyiLogRecharge(AllContextUtils.getLoginSysUser().getUserId(), AllContextUtils.getLoginSysUser().getUsername(), 4,
                    "采购订单取消",order.getTotalAmount(),accountBalance.getBalance(),accountBalance.getBalance().add(order.getTotalAmount()),
                    order.getOrderNo(), cancelReason,
                    AllContextUtils.getLoginSysUser().getUsername(), "系统", accountBalance.getCurrency());

            accountBalance.setBalance(accountBalance.getBalance().add(order.getTotalAmount()));
            accountBalanceMapper.updateById(accountBalance);
        }
        return false;
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
        // 处理receiptImages，将文件名数组转换为MinIO访问链接
        if (purchaserOrderVO.getReceiptImages() != null && !purchaserOrderVO.getReceiptImages().isEmpty()) {
            try {
                // 解析JSON字符串为文件名数组
                String[] fileNames = purchaserOrderVO.getReceiptImages()
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .split(",");

                // 转换为MinIO预览链接数组
                List<String> previewUrls = new ArrayList<>();
                for (String fileName : fileNames) {
                    if (fileName != null && !fileName.trim().isEmpty()) {
                        String previewUrl = minioUtil.getPreviewUrl(fileName.trim());
                        if (previewUrl != null) {
                            previewUrls.add(previewUrl);
                        }
                    }
                }

                // 将预览链接数组转换为JSON字符串存储到VO中
                if (!previewUrls.isEmpty()) {
                    StringBuilder urlJson = new StringBuilder("[");
                    for (int i = 0; i < previewUrls.size(); i++) {
                        if (i > 0) {
                            urlJson.append(",");
                        }
                        urlJson.append("\"").append(previewUrls.get(i)).append("\"");
                    }
                    urlJson.append("]");
                    purchaserOrderVO.setReceiptImages(urlJson.toString());
                }
            } catch (Exception e) {
                log.error("处理receiptImages失败:", e);
                purchaserOrderVO.setReceiptImages("[]");
            }
        } else {
            purchaserOrderVO.setReceiptImages("[]");
        }
        purchaserOrderVO.getOrderItems().forEach(item -> item.setProductImage(minioUtil.getPreviewUrl(item.getProductImage())));
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
        exportData.forEach(vo -> vo.setProductImage(minioUtil.getPreviewUrl(vo.getProductImage())));
        // 导出Excel（使用支持合并单元格的方法）
        String fileName = "采购订单数据";
        String sheetName = "订单列表";
        ExcelUtils.exportExcelWithMerge(response, exportData, fileName, sheetName, PurchaserOrderExportVO.class);
    }
}




