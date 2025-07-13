package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.annotation.OperationLog;
import elec.shop.pojo.purchase.*;
import elec.shop.pojo.purchase.dto.PurchaserOrderDTO;
import elec.shop.pojo.purchase.dto.PurchaserOrderQueryDTO;
import elec.shop.pojo.purchase.dto.PurchaserTaskQueryDTO;
import elec.shop.pojo.purchase.dto.PurchaserQueryDTO;
import elec.shop.pojo.purchase.vo.PurchaserOrderVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.pojo.sys.dto.PurchaseInfoVO;
import elec.shop.service.purchase.*;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import elec.shop.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "采购管理")
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

    private final ShopInfoService shopInfoService;
    private final AccountBalanceService accountBalanceService;
    private final FinanceAccountService financeAccountService;
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaserInfoService purchaserInfoService;
    private final PurchaseTaskService purchaseTaskService;
    private final SysUserService sysUserService;

    @PostMapping("/createOrder")
    @ApiOperation("创建采购订单")
//    @OperationLog(
//        module = "采购管理",
//        operationType = "创建订单",
//        description = "创建采购订单",
//        isPurchaseOrder = true,
//        saveRequestData = true
//    )
    public Result<Object> createOrder(@RequestBody PurchaserOrderDTO purchaserOrderDTO) {
        return purchaseOrderService.createOrder(purchaserOrderDTO);
    }

    @PostMapping("/orders")
    @ApiOperation("查询当前用户下的采购订单")
    @OperationLog(module = "采购管理", operationType = "查询订单", description = "查询当前用户下的采购订单")
    public Result queryOrders(
            @ApiParam("查询参数") @RequestBody PurchaserOrderQueryDTO query
    ) {
        try {
            // 参数验证
            if (query == null) {
                return Result.fail().message("查询参数不能为空");
            }
            if (query.getPage() == null || query.getPage() < 1) {
                query.setPage(1);
            }
            if (query.getSize() == null || query.getSize() < 1) {
                query.setSize(10);
            }
            // 限制每页最大条数
            if (query.getSize() > 100) {
                query.setSize(100);
            }

            // 日期格式验证
            if (StringUtils.isNotBlank(query.getStartTime()) && !query.getStartTime().matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return Result.fail().message("开始时间格式不正确");
            }
            if (StringUtils.isNotBlank(query.getEndTime()) && !query.getEndTime().matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return Result.fail().message("结束时间格式不正确");
            }

            // 调用服务层查询数据
            IPage<PurchaserOrderVO> orderPage = purchaseOrderService.queryUserOrders(query);

            if (orderPage == null || orderPage.getRecords().isEmpty()) {
                return Result.ok().message("暂无数据");
            }

            return Result.ok(orderPage);
        } catch (BusinessException be) {
            log.warn("查询订单失败: {}", be.getMessage());
            return Result.fail().message(be.getMessage());
        } catch (Exception e) {
            log.error("查询订单异常", e);
            return Result.fail().message("系统异常，请稍后重试");
        }
    }

    @PostMapping("/orderInfo")
    @ApiOperation("查询采购订单详情")
    @OperationLog(module = "采购管理", operationType = "查询订单", description = "查询采购订单详情")
    public Result<Object> orderInfo(
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
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    // @OperationLog(module = "采购管理", operationType = "导出订单EXCEL", description = "导出订单EXCEL")
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
    public Result<Object> purchasersAll() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        loginSysUser.setUserType(sysUserService.getById(loginSysUser.getUserId()).getUserType());
        if (!loginSysUser.getUserType().equals(1) && !loginSysUser.getUserType().equals(2)) return Result.fail().message("无权操作");
        List<PurchaseInfoVO> purchaseInfoVOList = purchaserInfoService.selectPurchaserInfoList();
        return Result.ok(purchaseInfoVOList);
    }

    @PostMapping("/purchaser/add")
    @ApiOperation("新增采购员")
    @OperationLog(module = "采购管理", operationType = "新增采购员", description = "新增采购员")
    public Result<Object> addPurchaser(@ApiParam("用户ID") @RequestParam Long userId) {
        PurchaserInfo purchaserInfo = purchaserInfoService.addPurchaser(userId);
        return Result.ok(purchaserInfo);
    }

    @GetMapping("/tasks")
    @ApiOperation("查询采购任务列表")
    @OperationLog(module = "采购管理", operationType = "查询任务", description = "查询采购任务列表")
    public Result<Page<PurchaseTask>> queryTasks(
            @ApiParam("查询参数") @RequestBody PurchaserTaskQueryDTO query
    ) {
        Page<PurchaseTask> taskPage = purchaseTaskService.queryTasks(query);
        return Result.ok(taskPage);
    }

    @PostMapping("/task/assign")
    @ApiOperation("分配采购任务")
    @OperationLog(module = "采购管理", operationType = "分配任务", description = "分配采购任务")
    public Result<Object> assignTask(
            @ApiParam("任务ID") @RequestParam Long taskId,
            @ApiParam("采购员ID") @RequestParam Long purchaserId
    ) {
        purchaseTaskService.assignTask(taskId, purchaserId);
        return Result.ok();
    }

    @GetMapping("/test")
    @ApiOperation("测试汇率")
    public Result<Object> test(@RequestParam("userId") Long userId) {
        shopInfoService.test(userId);
        return Result.ok();
    }
}
