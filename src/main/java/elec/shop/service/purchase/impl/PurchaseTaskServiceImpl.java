package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.purchase.*;
import elec.shop.pojo.purchase.*;
import elec.shop.pojo.purchase.dto.PurchaserTaskQueryDTO;
import elec.shop.pojo.purchase.dto.TaskStatusChangeDTO;
import elec.shop.pojo.purchase.vo.PurchaserOrderItemVO;
import elec.shop.pojo.purchase.vo.PurchaserTaskVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.PurchaseOrderItemService;
import elec.shop.service.purchase.PurchaseOrderService;
import elec.shop.service.purchase.PurchaseTaskService;
import elec.shop.service.purchase.PurchaserInfoService;
import elec.shop.service.sys.SysUserService;
import elec.shop.strategy.factory.OrderStatusMessageHandlerFactory;
import elec.shop.strategy.inter.OrderStatusMessageHandler;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【purchase_task(采购任务表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
public class PurchaseTaskServiceImpl extends ServiceImpl<PurchaseTaskMapper, PurchaseTask>
        implements PurchaseTaskService {

    private final PurchaserInfoService purchaserInfoService;
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaserInfoMapper purchaserInfoMapper;
    private final PurchaseOrderItemService purchaseOrderItemService;
    private final SysUserService sysUserService;
    private final EmailUtil emailUtil;
    private final OrderStatusMessageHandlerFactory handlerFactory;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final FinanceAccountMapper financeAccountMapper;
    private final AccountBalanceMapper accountBalanceMapper;

    @Override
    public Page<PurchaseTask> queryTasks(PurchaserTaskQueryDTO query) {
        LambdaQueryWrapper<PurchaseTask> wrapper = new LambdaQueryWrapper<>();

        if (query.getTaskStatus() != null) {
            wrapper.eq(PurchaseTask::getTaskStatus, query.getTaskStatus());
        }
        if (query.getPriority() != null) {
            wrapper.eq(PurchaseTask::getPriority, query.getPriority());
        }

        wrapper.orderByDesc(PurchaseTask::getPriority)
               .orderByDesc(PurchaseTask::getCreatedAt);

        return page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(Long taskId, Long purchaserId) {
        // 检查采购员是否存在
        if (!purchaserInfoService.isPurchaser(purchaserId)) {
            throw new RuntimeException("采购员不存在");
        }

        PurchaseOrder task = purchaseOrderService.getById(taskId);
        if (task == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查任务状态
        if (task.getOrderStatus() != 0) {
            throw new RuntimeException("只有待分配的任务可以分配");
        }

        task.setPurchaserId(purchaserId);
        task.setOrderStatus(1);
        purchaseOrderService.updateById(task);

        PurchaseTask purchaseTask = new PurchaseTask();
        purchaseTask.setTaskId(taskId);
        purchaseTask.setTaskCode(AllContextUtils.generatePurchaseTaskNumber(purchaserId));
        purchaseTask.setPurchaserId(purchaserId);
        purchaseTask.setTaskType(1);
        purchaseTask.setTaskStatus(0);
        purchaseTask.setPriority(2);
        purchaseTask.setTitle(task.getOrderNo());
        purchaseTask.setContent("采购订单");
        purchaseTask.setStartTime(new Date());

        // 发送消息给采购员
        PurchaserInfo byId1 = purchaserInfoService.getById(purchaserId);
        SysUser byId2 = sysUserService.getById(byId1.getUserId());
        emailUtil.sendCustomEmail(byId2.getEmail(),"采购任务通知","您有一个新的采购任务需要处理，请及时处理。");

        // 发送消息给用户
        PurchaseOrder byId = purchaseOrderService.getById(taskId);
        SysUser user = sysUserService.getById(byId.getUserId());
        emailUtil.sendCustomEmail(user.getEmail(),"采购订单通知",
                "您的"+byId.getOrderNo()+"采购订单已被分配给对应的采购员。");
        this.save(purchaseTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTask(Long taskId) {
        PurchaseTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 检查任务状态
        if (task.getTaskStatus() != 0) {
            throw new RuntimeException("只有待处理的任务可以开始");
        }

        task.setTaskStatus(1); // 处理中
        task.setStartTime(new Date());
        updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId) {
        PurchaseTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 检查任务状态
        if (task.getTaskStatus() != 1) {
            throw new RuntimeException("只有处理中的任务可以完成");
        }

        task.setTaskStatus(2); // 已完成
        task.setCompleteTime(new Date());
        updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId, String reason) {
        PurchaseTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 检查任务状态
        if (task.getTaskStatus() == 2) {
            throw new RuntimeException("已完成的任务不能取消");
        }

        task.setTaskStatus(3); // 已取消
        task.setContent(reason); // 使用content字段存储取消原因
        updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseTask createTask(PurchaseTask task) {
        // 生成任务编号：T + 时间戳后8位 + 4位随机数
        String taskCode = "T" + String.format("%08d", System.currentTimeMillis() % 100000000)
                + String.format("%04d", (int)(Math.random() * 10000));
        task.setTaskCode(taskCode);

        // 设置初始状态
        task.setTaskStatus(0); // 待处理
        if (task.getPriority() == null) {
            task.setPriority(1); // 默认低优先级
        }

        save(task);
        return task;
    }

    @Override
    public IPage<PurchaserTaskVO> queryMyPurchaseTask(PurchaserTaskQueryDTO query) {
        // 获取当前登录用户
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();

        // 查询采购员信息
        PurchaserInfo purchaserInfo = purchaserInfoMapper.selectOne(
                new LambdaQueryWrapper<PurchaserInfo>()
                        .eq(PurchaserInfo::getUserId, loginSysUser.getUserId())
        );
        if (purchaserInfo == null) {
            return new Page<>(query.getPage(), query.getSize());
        }

        // 1. 构建主表查询条件
        LambdaQueryWrapper<PurchaseTask> wrapper = new LambdaQueryWrapper<PurchaseTask>()
                .eq(PurchaseTask::getPurchaserId, purchaserInfo.getPurchaserId())
                .eq(PurchaseTask::getIsDeleted, 0);

        // 添加动态查询条件
        if (query.getTaskStatus() != null) {
            wrapper.eq(PurchaseTask::getTaskStatus, query.getTaskStatus());
        }
        if (query.getPriority() != null) {
            wrapper.eq(PurchaseTask::getPriority, query.getPriority());
        }

        // 自定义排序规则
        wrapper.last("ORDER BY CASE WHEN task_status = 4 THEN 1 ELSE 0 END, created_at DESC");

        // 2. 主表分页查询
        Page<PurchaseTask> page = new Page<>(query.getPage(), query.getSize());
        IPage<PurchaseTask> taskPage = this.page(page, wrapper);

        // 3. 提前处理空结果
        List<Long> taskIds = taskPage.getRecords().stream()
                .map(PurchaseTask::getTaskId)
                .collect(Collectors.toList());
        if (taskIds.isEmpty()) {
            return new Page<>(query.getPage(), query.getSize());
        }

        // 4. 查询订单数据（必须保留）
        List<PurchaseOrder> orders = purchaseOrderService.list(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .in(PurchaseOrder::getOrderId, taskIds)
        );
        Map<Long, PurchaseOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(PurchaseOrder::getOrderId, order -> order));

        // 5. 查询订单项数据
        List<PurchaseOrderItem> orderItems = purchaseOrderItemService.list(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .in(PurchaseOrderItem::getOrderId, taskIds)
        );
        Map<Long, List<PurchaseOrderItem>> itemMap = orderItems.stream()
                .collect(Collectors.groupingBy(PurchaseOrderItem::getOrderId));

        // 6. 组装结果
        List<PurchaserTaskVO> taskVOS = taskPage.getRecords().stream().map(task -> {
            PurchaserTaskVO vo = new PurchaserTaskVO();

            // 获取关联的订单信息（确保任务ID与订单ID匹配）
            PurchaseOrder order = orderMap.get(task.getTaskId());
            if (order != null) {
                // 从订单表获取必要字段（根据实际VO字段调整）
                BeanUtils.copyProperties(order, vo);
            }

            BeanUtils.copyProperties(task, vo);

            // 设置采购员名称
            vo.setPurchaserName(sysUserService
                    .getById(loginSysUser
                            .getUserId())
                    .getRealName());

            // 设置订单项
            List<PurchaseOrderItem> items = itemMap.getOrDefault(task.getTaskId(), Collections.emptyList());
            vo.setOrderItems(items.stream().map(item -> {
                PurchaserOrderItemVO itemVO = new PurchaserOrderItemVO();
                BeanUtils.copyProperties(item, itemVO);
                return itemVO;
            }).collect(Collectors.toList()));

            return vo;
        }).collect(Collectors.toList());

        // 7. 组装分页结果
        Page<PurchaserTaskVO> resultPage = new Page<>(query.getPage(), query.getSize(), taskPage.getTotal());
        resultPage.setRecords(taskVOS);

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean taskStatusChange(TaskStatusChangeDTO dto) {
        // 1. 获取当前用户信息
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();

        // 2. 查询当前用户对应的采购员信息
        PurchaserInfo purchaserInfo = purchaserInfoMapper.selectOne(
                new LambdaQueryWrapper<PurchaserInfo>()
                        .eq(PurchaserInfo::getUserId, loginSysUser.getUserId())
                        .eq(PurchaserInfo::getIsDeleted, 0) // 添加逻辑删除过滤
        );

        // 3. 查询任务信息并校验任务存在性
        PurchaseTask task = this.getById(dto.getTaskId());

        // 4. 校验当前用户是否有权限操作该任务
        if (!task.getPurchaserId().equals(purchaserInfo.getPurchaserId())) {
            throw new BusinessException("无权限操作该任务");
        }

        // 5. 校验状态变更合法性
        if (!isValidStatusChange(task.getTaskStatus(), dto.getNewStatus())) {
            throw new BusinessException("不允许的状态变更");
        }

        // 6. 校验价格是否存在变化
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectOne(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getOrderNo, task.getTitle())
        );
        if (dto.getRealTotalAmount() != null && !dto.getRealTotalAmount().equals(purchaseOrder.getTotalAmount())) {
            int i = dto.getRealTotalAmount().compareTo(purchaseOrder.getRealTotalAmount());
            SysUser byId = sysUserService.getById(purchaseOrder.getUserId());
            if (i>0){
                purchaseOrder.setRealTotalAmount(dto.getRealTotalAmount());
                purchaseOrder.setPaymentStatus(0);
                emailUtil.sendCustomEmail(
                        byId.getEmail(),
                        "价格偏差",
                        "订单差额，快去补齐差额以方便订单正常运作"
                );
                purchaseOrderMapper.updateById(purchaseOrder);
                task.setTaskStatus(5);
            } else if (i<0){
                purchaseOrder.setTotalAmount(dto.getRealTotalAmount());
                purchaseOrder.setRealTotalAmount(dto.getRealTotalAmount());
                emailUtil.sendCustomEmail(
                        sysUserService.getById(purchaseOrder.getUserId()).getEmail(),
                        "价格偏差",
                        "订单金额偏多，差价已给你补齐"
                );
                purchaseOrderMapper.updateById(purchaseOrder);
                task.setTaskStatus(2);

                String currency = purchaseOrder.getCurrency();
                FinanceAccount financeAccount = financeAccountMapper.selectOne(new LambdaQueryWrapper<FinanceAccount>()
                        .eq(FinanceAccount::getUserId, byId.getUserId()));

                AccountBalance accountBalance = accountBalanceMapper.selectOne(new LambdaQueryWrapper<AccountBalance>()
                        .eq(AccountBalance::getAccountId, financeAccount.getAccountId())
                        .eq(AccountBalance::getCurrency, currency));
                accountBalance.setAccountId(financeAccount.getAccountId());
                accountBalance.setBalance(accountBalance.getBalance().add(BigDecimal.valueOf(i)));
                accountBalanceMapper.updateById(accountBalance);
            }
        }else {
            // 7. 更新任务状态
            task.setTaskStatus(dto.getNewStatus());
        }

        boolean success = this.updateById(task);

        // 8. 触发关联操作（如果有）
        if (success && dto.getNewStatus() == 3) { // 任务完成状态
            updateRelatedOrderStatus(task.getTaskId(), 4); // 更新关联订单为已完成
        }

        // 9. 更新回填单号和采购备注，如果有
        if (dto.getRemarkOrderNo() != null) {
            purchaseOrder.setRemarkOrderNo(dto.getRemarkOrderNo());
        }
        if (dto.getRemark() != null) {
            purchaseOrder.setRemark(dto.getRemark());
        }
        purchaseOrderMapper.updateById(purchaseOrder);
        return success;
    }

    @Override
    public PurchaserTaskVO getTaskInfo(Long taskId) {
        // 1. 查询任务基本信息
        PurchaseTask task = this.getById(taskId);
        if (task == null || task.getIsDeleted() == 1) {
            throw new BusinessException("任务不存在或已删除");
        }

        // 2. 查询关联的订单信息
        PurchaseOrder order = purchaseOrderService.getOne(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getOrderId, taskId)
                        .eq(PurchaseOrder::getIsDeleted, 0)
        );
        if (order == null) {
            throw new BusinessException("关联的订单不存在");
        }

        // 3. 查询订单项信息
        List<PurchaseOrderItem> orderItems = purchaseOrderItemService.list(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, taskId)
                        .eq(PurchaseOrderItem::getIsDeleted, 0)
        );

        // 4. 查询采购员信息
        PurchaserInfo purchaserInfo = purchaserInfoService.getOne(
                new LambdaQueryWrapper<PurchaserInfo>()
                        .eq(PurchaserInfo::getPurchaserId, task.getPurchaserId())
                        .eq(PurchaserInfo::getIsDeleted, 0)
        );
        SysUser purchaser = null;
        if (purchaserInfo != null) {
            purchaser = sysUserService.getById(purchaserInfo.getUserId());
        }

        // 5. 组装VO对象
        PurchaserTaskVO vo = new PurchaserTaskVO();
        // 复制任务基本信息
        BeanUtils.copyProperties(task, vo);
        // 复制订单信息
        BeanUtils.copyProperties(order, vo);

        // 设置额外信息
        vo.setPurchaserName(purchaser != null ? purchaser.getRealName() : "未知");
        vo.setOrderItems(orderItems.stream().map(item -> {
            PurchaserOrderItemVO itemVO = new PurchaserOrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 校验状态变更是否合法
     */
    private boolean isValidStatusChange(Integer oldStatus, Integer newStatus) {
        // 示例状态转换规则：
        // 0(待处理) -> 1(处理中)
        // 1(处理中) -> 2(已完成) 或 3(已取消)
        // 其他状态不允许变更
        return switch (oldStatus) {
            case 0 -> newStatus == 1;
            case 1 -> newStatus == 2 || newStatus == 3;
            default -> false;
        };
    }

    /**
     * 更新关联订单状态
     */
    private void updateRelatedOrderStatus(Long orderId, Integer newStatus) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(orderId);
        order.setOrderStatus(newStatus);
        order.setCompleteTime(new Date());

        purchaseOrderService.updateById(order);

        PurchaseOrder byId = purchaseOrderService.getById(orderId);

        String receiver = sysUserService.getById(byId.getUserId())
                .getEmail();

        // 获取对应的消息处理器
        OrderStatusMessageHandler handler = handlerFactory.getHandler(newStatus);

        if (handler != null) {
            // 获取状态变更消息
            String message = handler.getMessage(byId.getOrderNo());

            // 发送邮件通知
            emailUtil.sendCustomEmail(
                    receiver,
                    "订单状态更新通知",
                    message
            );
        }
    }
}




