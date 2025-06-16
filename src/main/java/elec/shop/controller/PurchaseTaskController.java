package elec.shop.controller;

import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "采购任务管理")
@RestController
@RequestMapping("/purchase-task")
@RequiredArgsConstructor
public class PurchaseTaskController {

    // 目前如果查询我的采购任务，会牵涉到4表
    // 如果分页可能会存在分页不准确的问题
    @ApiOperation("查询我的采购任务(分页/搜索)")
    public Result queryMyPurchaseTask() {
        return Result.ok();
    }

    @ApiOperation("采购任务状态变化")
    public Result taskStatusChange() {
        return Result.ok();
    }

    @ApiOperation("采购任务详情")
    public Result taskInfo() {
        return Result.ok();
    }
}
