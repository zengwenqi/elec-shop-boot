package elec.shop.service.purchase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.PurchaseTask;
import elec.shop.pojo.purchase.dto.PurchaseTaskQueryDTO;

/**
* @author Lenovo
* @description 针对表【purchase_task(采购任务表)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface PurchaseTaskService extends IService<PurchaseTask> {
    /**
     * 查询采购任务列表
     */
    Page<PurchaseTask> queryTasks(PurchaseTaskQueryDTO query);

    /**
     * 分配采购任务
     */
    void assignTask(Long taskId, Long purchaserId);

    /**
     * 开始任务
     */
    void startTask(Long taskId);

    /**
     * 完成任务
     */
    void completeTask(Long taskId);

    /**
     * 取消任务
     */
    void cancelTask(Long taskId, String reason);

    /**
     * 创建采购任务
     */
    PurchaseTask createTask(PurchaseTask task);
}
