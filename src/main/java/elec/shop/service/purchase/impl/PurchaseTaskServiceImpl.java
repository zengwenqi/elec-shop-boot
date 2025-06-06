package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.PurchaseTaskMapper;
import elec.shop.pojo.purchase.PurchaseTask;
import elec.shop.pojo.purchase.dto.PurchaseTaskQueryDTO;
import elec.shop.service.purchase.PurchaseTaskService;
import elec.shop.service.purchase.PurchaserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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

    @Override
    public Page<PurchaseTask> queryTasks(PurchaseTaskQueryDTO query) {
        LambdaQueryWrapper<PurchaseTask> wrapper = new LambdaQueryWrapper<>();

        if (query.getTaskStatus() != null) {
            wrapper.eq(PurchaseTask::getTaskStatus, query.getTaskStatus());
        }
        if (query.getPriority() != null) {
            wrapper.eq(PurchaseTask::getPriority, query.getPriority());
        }
        if (query.getPurchaserId() != null) {
            wrapper.eq(PurchaseTask::getPurchaserId, query.getPurchaserId());
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

        PurchaseTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 检查任务状态
        if (task.getTaskStatus() != 0) {
            throw new RuntimeException("只有待处理的任务可以分配");
        }

        task.setPurchaserId(purchaserId);
        updateById(task);
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
}




