package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.balance.FinanceTransaction;
import elec.shop.pojo.balance.dto.RechargeRequestDTO;
import elec.shop.pojo.balance.vo.RechargeRecordVO;
import elec.shop.service.balance.FinanceTransactionService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@Api(tags = "充值管理")
@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
@Slf4j
public class RechargeController {

    private final FinanceTransactionService financeTransactionService;

    @PostMapping("/recharge")
    @ApiOperation("提交充值申请")
    @DataSource(DataSourceType.MASTER)
    public Result submitRecharge(@RequestBody RechargeRequestDTO rechargeRequest) {
        try {
            // 获取当前登录用户
            Long userId = AllContextUtils.getLoginSysUser().getUserId();

            log.info("用户 {} 提交充值申请: 金额={}, 币种={}, 支付方式={}",
                    userId, rechargeRequest.getAmount(), rechargeRequest.getCurrency(), rechargeRequest.getPaymentMethod());

            // 创建充值记录（待审核状态）
            Long transactionId = financeTransactionService.createRechargeRecord(userId, rechargeRequest);

            log.info("充值申请创建成功，交易ID: {}", transactionId);

            Map<String, Object> data = Map.of(
                    "transactionId", transactionId,
                    "amount", rechargeRequest.getAmount(),
                    "currency", rechargeRequest.getCurrency(),
                    "paymentMethod", rechargeRequest.getPaymentMethod()
            );

            return Result.ok(data).message("充值申请提交成功，请等待审核");
        } catch (Exception e) {
            log.error("充值申请失败", e);
            return Result.fail().message("充值申请失败：" + e.getMessage());
        }
    }

    @GetMapping("/recharge/list")
    @ApiOperation("查询充值记录列表")
    @DataSource(DataSourceType.SLAVE)
    public Result getRechargeList(
            @ApiParam("订单号") @RequestParam(required = false) String orderNo,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("支付方式") @RequestParam(required = false) String paymentMethod,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 获取当前登录用户
            Long userId = AllContextUtils.getLoginSysUser().getUserId();

            log.info("用户 {} 查询充值记录: 页码={}, 每页大小={}", userId, page, size);

            IPage<RechargeRecordVO> result = financeTransactionService.getRechargeList(
                    userId, orderNo, status, paymentMethod, startTime, endTime, page, size);

            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询充值记录失败", e);
            return Result.fail().message("查询充值记录失败：" + e.getMessage());
        }
    }

    @GetMapping("/recharge/{transactionId}")
    @ApiOperation("获取充值记录详情")
    @DataSource(DataSourceType.SLAVE)
    public Result getRechargeDetail(@PathVariable String transactionId) {
        try {
            // 获取当前登录用户
            Long userId = AllContextUtils.getLoginSysUser().getUserId();

            log.info("用户 {} 查询充值记录详情: transactionId={}", userId, transactionId);

            RechargeRecordVO detail = financeTransactionService.getRechargeDetail(userId, transactionId);
            if (detail == null) {
                return Result.fail().message("充值记录不存在");
            }

            return Result.ok(detail);
        } catch (Exception e) {
            log.error("查询充值记录详情失败", e);
            return Result.fail().message("查询充值记录详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/recharge/{transactionId}/cancel")
    @ApiOperation("取消充值申请")
    @DataSource(DataSourceType.MASTER)
    public Result cancelRecharge(@PathVariable String transactionId) {
        try {
            // 获取当前登录用户
            Long userId = AllContextUtils.getLoginSysUser().getUserId();

            log.info("用户 {} 取消充值申请: transactionId={}", userId, transactionId);

            boolean success = financeTransactionService.cancelRecharge(userId, transactionId);
            if (!success) {
                return Result.fail().message("取消充值失败，请检查充值状态");
            }

            return Result.ok().message("充值申请已取消");
        } catch (Exception e) {
            log.error("取消充值申请失败", e);
            return Result.fail().message("取消充值申请失败：" + e.getMessage());
        }
    }

    @PostMapping("/recharge/appeal")
    @ApiOperation("申诉充值")
    @DataSource(DataSourceType.MASTER)
    public Result appealRecharge(@RequestBody Map<String, Object> appealData) {
        try {
            // 获取当前登录用户
            Long userId = AllContextUtils.getLoginSysUser().getUserId();

            String transactionId = (String) appealData.get("transactionId");
            String type = (String) appealData.get("type");
            String description = (String) appealData.get("description");
            String contact = (String) appealData.get("contact");

            log.info("用户 {} 提交充值申诉: transactionId={}, type={}", userId, transactionId, type);

            boolean success = financeTransactionService.appealRecharge(userId, transactionId, type, description, contact);
            if (!success) {
                return Result.fail().message("申诉提交失败，请检查充值记录状态");
            }

            return Result.ok().message("申诉提交成功，我们会尽快处理");
        } catch (Exception e) {
            log.error("申诉充值失败", e);
            return Result.fail().message("申诉充值失败：" + e.getMessage());
        }
    }

    @PutMapping("/recharge/approve/{transactionId}")
    @ApiOperation("管理员审核通过充值")
    @DataSource(DataSourceType.MASTER)
    public Result approveRecharge(@PathVariable String transactionId,@RequestBody String remark) {
        try {
            // 获取当前登录用户
            Long adminUserId = AllContextUtils.getLoginSysUser().getUserId();

            log.info("管理员 {} 审核通过充值: transactionId={}", adminUserId, transactionId);

            boolean success = financeTransactionService.approveRecharge(transactionId, adminUserId,remark);
            if (!success) {
                return Result.fail().message("审核失败，请检查充值记录状态");
            }

            return Result.ok().message("充值审核通过");
        } catch (Exception e) {
            log.error("审核充值失败", e);
            return Result.fail().message("审核充值失败：" + e.getMessage());
        }
    }

    @PostMapping("/recharge/reject/{transactionId}")
    @ApiOperation("管理员审核拒绝充值")
    @DataSource(DataSourceType.MASTER)
    public Result rejectRecharge(@PathVariable String transactionId, @RequestBody Map<String, String> rejectData) {
        try {
            // 获取当前登录用户
            Long adminUserId = AllContextUtils.getLoginSysUser().getUserId();
            String reason = rejectData.get("reason");

            log.info("管理员 {} 审核拒绝充值: transactionId={}, reason={}", adminUserId, transactionId, reason);

            boolean success = financeTransactionService.rejectRecharge(transactionId, adminUserId, reason);
            if (!success) {
                return Result.fail().message("审核失败，请检查充值记录状态");
            }

            return Result.ok().message("充值审核拒绝");
        } catch (Exception e) {
            log.error("审核拒绝充值失败", e);
            return Result.fail().message("审核拒绝充值失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/recharge/batch/approve")
    @ApiOperation("管理员批量审核通过充值")
    @DataSource(DataSourceType.MASTER)
    public Result batchApproveRecharge(@RequestBody Map<String, List<String>> requestData) {
        try {
            // 获取当前登录用户
            Long adminUserId = AllContextUtils.getLoginSysUser().getUserId();
            List<String> transactionIds = requestData.get("transactionIds");

            log.info("管理员 {} 批量审核通过充值: transactionIds={}", adminUserId, transactionIds);

            int successCount = financeTransactionService.batchApproveRecharge(transactionIds, adminUserId);

            return Result.ok().message("批量审核完成，成功处理 " + successCount + " 条记录");
        } catch (Exception e) {
            log.error("批量审核通过充值失败", e);
            return Result.fail().message("批量审核通过充值失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/recharge/batch/reject")
    @ApiOperation("管理员批量审核拒绝充值")
    @DataSource(DataSourceType.MASTER)
    public Result batchRejectRecharge(@RequestBody Map<String, Object> requestData) {
        try {
            // 获取当前登录用户
            Long adminUserId = AllContextUtils.getLoginSysUser().getUserId();
            @SuppressWarnings("unchecked")
            List<String> transactionIds = (List<String>) requestData.get("transactionIds");
            String reason = (String) requestData.get("reason");

            log.info("管理员 {} 批量审核拒绝充值: transactionIds={}, reason={}", adminUserId, transactionIds, reason);

            int successCount = financeTransactionService.batchRejectRecharge(transactionIds, adminUserId, reason);

            return Result.ok().message("批量审核完成，成功处理 " + successCount + " 条记录");
        } catch (Exception e) {
            log.error("批量审核拒绝充值失败", e);
            return Result.fail().message("批量审核拒绝充值失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/recharge/pending")
    @ApiOperation("获取待审核充值列表")
    @DataSource(DataSourceType.SLAVE)
    public Result getPendingRechargeList(
            @ApiParam("订单号") @RequestParam(required = false) String orderNo,
            @ApiParam("支付方式") @RequestParam(required = false) String paymentMethod,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("管理员查询待审核充值记录: 页码={}, 每页大小={}", page, size);

            IPage<RechargeRecordVO> result = financeTransactionService.getPendingRechargeList(
                    orderNo, paymentMethod, startTime, endTime, page, size);

            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询待审核充值记录失败", e);
            return Result.fail().message("查询待审核充值记录失败：" + e.getMessage());
        }
    }
}
