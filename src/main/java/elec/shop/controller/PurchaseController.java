package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.annotation.OperationLog;
import elec.shop.pojo.purchase.*;
import elec.shop.pojo.purchase.dto.PurchaseOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaseOrderQueryDTO;
import elec.shop.pojo.purchase.dto.PurchaseTaskQueryDTO;
import elec.shop.pojo.purchase.dto.PurchaserQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaseOrderVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.*;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;

@Api(tags = "采购管理")
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final ShopInfoService shopInfoService;
    private final AccountBalanceService accountBalanceService;
    private final FinanceAccountService financeAccountService;
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaserInfoService purchaserInfoService;
    private final PurchaseTaskService purchaseTaskService;

    @PostMapping("/createOrder")
    @ApiOperation("创建采购订单")
//    @OperationLog(
//        module = "采购管理",
//        operationType = "创建订单",
//        description = "创建采购订单",
//        isPurchaseOrder = true,
//        saveRequestData = true
//    )
    public Result createOrder(@RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        return purchaseOrderService.createOrder(purchaseOrderDTO);
    }

    @PostMapping("/orders")
    @ApiOperation("查询当前用户下的采购订单")
    @OperationLog(module = "采购管理", operationType = "查询订单", description = "查询当前用户下的采购订单")
    public Result<IPage<PurchaseOrderVO>> queryOrders(
            @ApiParam("查询参数") @RequestBody PurchaseOrderQueryDTO query
    ) {
        IPage<PurchaseOrderVO> orderPage = purchaseOrderService.queryUserOrders(query);
        return Result.ok(orderPage);
    }

    @PostMapping("/orderInfo")
    @ApiOperation("查询采购订单详情")
    @OperationLog(module = "采购管理", operationType = "查询订单", description = "查询采购订单详情")
    public Result orderInfo(
            @RequestParam("orderId") Long orderId
    ) {
        return Result.ok(purchaseOrderService.orderInfo(orderId));
    }

    @PostMapping("/cancelOrder")
    @ApiOperation("取消订单")
    @OperationLog(module = "采购管理", operationType = "取消订单", description = "取消订单")
    public Result cancelOrder(
            @RequestParam("orderId") Long orderId,
            @RequestParam("cancelReason") String cancelReason
    ) {
        return Result.ok(purchaseOrderService.cancelOrder(orderId,cancelReason));
    }

    @GetMapping("/exportOrders")
    @ApiOperation("导出订单EXCEL")
//    @OperationLog(module = "采购管理", operationType = "导出订单EXCEL", description = "导出订单EXCEL")
    public void exportOrders(
            @ApiParam("店铺ID") @RequestParam(required = false) Long shopId,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime,
            HttpServletResponse response
    ) {
        purchaseOrderService.exportOrders(shopId, startTime, endTime, response);
    }

    @GetMapping("/purchasers")
    @ApiOperation("查询采购员列表")
    @OperationLog(module = "采购管理", operationType = "查询采购员", description = "查询采购员列表")
    public Result<Page<PurchaserInfo>> queryPurchasers(
            @ApiParam("查询参数") @RequestBody PurchaserQueryDTO purchaserQueryDTO
    ) {
        Page<PurchaserInfo> purchaserPage = purchaserInfoService.queryPurchasers(purchaserQueryDTO);
        return Result.ok(purchaserPage);
    }

    @GetMapping("/purchasersAll")
    @ApiOperation("查询所有采购员")
    @OperationLog(module = "采购管理", operationType = "查询采购员", description = "查询所有采购员")
    public Result purchasersAll() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        if (loginSysUser.getUserType() != 1 || loginSysUser.getUserType() != 2) return Result.fail().message("无权操作");
        return Result.ok(purchaserInfoService.list());
    }

    @PostMapping("/purchaser/add")
    @ApiOperation("新增采购员")
    @OperationLog(module = "采购管理", operationType = "新增采购员", description = "新增采购员")
    public Result addPurchaser(@ApiParam("用户ID") @RequestParam Long userId) {
        PurchaserInfo purchaserInfo = purchaserInfoService.addPurchaser(userId);
        return Result.ok(purchaserInfo);
    }

    @GetMapping("/tasks")
    @ApiOperation("查询采购任务列表")
    @OperationLog(module = "采购管理", operationType = "查询任务", description = "查询采购任务列表")
    public Result<Page<PurchaseTask>> queryTasks(
            @ApiParam("查询参数") @RequestBody PurchaseTaskQueryDTO query
    ) {
        Page<PurchaseTask> taskPage = purchaseTaskService.queryTasks(query);
        return Result.ok(taskPage);
    }

    @PostMapping("/task/assign")
    @ApiOperation("分配采购任务")
    @OperationLog(module = "采购管理", operationType = "分配任务", description = "分配采购任务")
    public Result assignTask(
            @ApiParam("任务ID") @RequestParam Long taskId,
            @ApiParam("采购员ID") @RequestParam Long purchaserId
    ) {
        purchaseTaskService.assignTask(taskId, purchaserId);
        return Result.ok();
    }

    @GetMapping("/test")
    @ApiOperation("测试汇率")
    public Result test(@RequestParam("userId") Long userId) {
        shopInfoService.test(userId);
        return Result.ok();
    }
}
