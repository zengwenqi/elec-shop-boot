package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.purchase.dto.PurchaserTaskQueryDTO;
import elec.shop.pojo.purchase.dto.TaskStatusChangeDTO;
import elec.shop.pojo.purchase.vo.PurchaserTaskVO;
import elec.shop.service.purchase.PurchaseTaskService;
import elec.shop.utils.MinioUtil;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "采购任务管理")
@RestController
@RequestMapping("/purchase-task")
@RequiredArgsConstructor
public class PurchaseTaskController {

    private final PurchaseTaskService purchaseTaskService;
    private final MinioUtil minioUtil;

    // 目前如果查询我的采购任务，会牵涉到4表
    // 如果分页可能会存在分页不准确的问题
    @ApiOperation("查询我的采购任务(分页/搜索)")
    @PostMapping("/query")
    public Result<Object> queryMyPurchaseTask(@RequestBody PurchaserTaskQueryDTO query) {
        IPage<PurchaserTaskVO> taskPage = purchaseTaskService.queryMyPurchaseTask(query);
        return Result.ok(taskPage);
    }

    @ApiOperation("采购任务状态变化")
    @PostMapping("/status/change")
    public Result<Object> taskStatusChange(@RequestBody TaskStatusChangeDTO dto) {
        Boolean result = purchaseTaskService.taskStatusChange(dto);
        if (result)
            return Result.ok();
        return Result.fail().message("状态更新失败");
    }

    @ApiOperation("采购任务详情")
    @PostMapping("/info")
    public Result<Object> taskInfo(@RequestBody PurchaserTaskQueryDTO dto) {
        // 调用服务层获取任务详情
        PurchaserTaskVO taskVO = purchaseTaskService.getTaskInfo(dto.getTaskId());
        taskVO.getOrderItems().forEach(item -> item.setProductImage(minioUtil.getPreviewUrl(item.getProductImage())));
        return Result.ok(taskVO);
    }
}
