package elec.shop.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.annotation.DataSource;
import elec.shop.annotation.OperationLog;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.finance.MoneyLog;
import elec.shop.pojo.finance.dto.MoneyLogQueryDTO;
import elec.shop.pojo.finance.vo.MoneyLogExportVO;
import elec.shop.service.finance.MoneyLogService;
import elec.shop.utils.ExcelUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/finance/money-log")
@RequiredArgsConstructor
@Api(tags = "财务日志管理")
public class MoneyLogController {

    private final MoneyLogService moneyLogService;

    /**
     * 分页查询财务日志
     */
    @GetMapping("/page")
    @ApiOperation("分页查询财务日志")
    @PreAuthorize("hasPermission(null ,'superadmin')")
//    @OperationLog(module = "财务日志管理", operationType = "查询", description = "分页查询财务日志", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public Result<IPage<MoneyLog>> getMoneyLogPage(MoneyLogQueryDTO queryDTO) {
        try {
            IPage<MoneyLog> page = moneyLogService.getMoneyLogPage(queryDTO);
            return Result.ok(page);
        } catch (Exception e) {
            log.error("查询财务日志失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 查询财务日志详情
     */
    @GetMapping("/{logId}")
    @ApiOperation("查询财务日志详情")
    @PreAuthorize("hasPermission(null ,'superadmin')")
//    @OperationLog(module = "财务日志管理", operationType = "查询", description = "查询财务日志详情", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public Result<MoneyLog> getMoneyLogDetail(@PathVariable Long logId) {
        try {
            MoneyLog moneyLog = moneyLogService.getById(logId);
            if (moneyLog == null) {
                return Result.fail(null);
            }
            return Result.ok(moneyLog);
        } catch (Exception e) {
            log.error("查询财务日志详情失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 查询用户余额变动统计
     */
    @GetMapping("/stats/balance")
    @ApiOperation("查询用户余额变动统计")
    @PreAuthorize("hasPermission(null ,'superadmin')")
//    @OperationLog(module = "财务日志管理", operationType = "查询", description = "查询用户余额变动统计", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public Result<Map<String, Object>> getUserBalanceStats(@RequestParam Long userId,
                                                           @RequestParam(required = false) String startTime,
                                                           @RequestParam(required = false) String endTime) {
        try {
            Map<String, Object> stats = moneyLogService.getUserBalanceStats(userId, startTime, endTime);
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("查询用户余额统计失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 查询操作类型统计
     */
    @GetMapping("/stats/operation")
    @ApiOperation("查询操作类型统计")
    @PreAuthorize("hasPermission(null ,'superadmin')")
//    @OperationLog(module = "财务日志管理", operationType = "查询", description = "查询操作类型统计", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public Result<List<Map<String, Object>>> getOperationTypeStats(@RequestParam(required = false) String startTime,
                                                                   @RequestParam(required = false) String endTime) {
        try {
            List<Map<String, Object>> stats = moneyLogService.getOperationTypeStats(startTime, endTime);
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("查询操作类型统计失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 查询用户最新余额
     */
    @GetMapping("/balance/latest")
    @ApiOperation("查询用户最新余额")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'FINANCE', 'PURCHASER', 'MERCHANT')")
//    @OperationLog(module = "财务日志管理", operationType = "查询", description = "查询用户最新余额", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public Result<BigDecimal> getLatestBalance(@RequestParam Long userId,
                                              @RequestParam(defaultValue = "CNY") String currency) {
        try {
            BigDecimal balance = moneyLogService.getLatestBalance(userId, currency);
            return Result.ok(balance);
        } catch (Exception e) {
            log.error("查询用户最新余额失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 查询交易总额统计
     */
    @GetMapping("/stats/total")
    @ApiOperation("查询交易总额统计")
    @PreAuthorize("hasPermission(null ,'superadmin')")
//    @OperationLog(module = "财务日志管理", operationType = "查询", description = "查询交易总额统计", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public Result<Map<String, Object>> getTotalStats(@RequestParam(required = false) String startTime,
                                                     @RequestParam(required = false) String endTime) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 查询各种操作类型的总额
            BigDecimal rechargeTotal = moneyLogService.getTotalAmountByDateRange(startTime, endTime, "RECHARGE");
            BigDecimal purchaseTotal = moneyLogService.getTotalAmountByDateRange(startTime, endTime, "PURCHASE");
            BigDecimal refundTotal = moneyLogService.getTotalAmountByDateRange(startTime, endTime, "REFUND");
            BigDecimal commissionTotal = moneyLogService.getTotalAmountByDateRange(startTime, endTime, "COMMISSION");
            BigDecimal withdrawTotal = moneyLogService.getTotalAmountByDateRange(startTime, endTime, "WITHDRAW");
            BigDecimal transferTotal = moneyLogService.getTotalAmountByDateRange(startTime, endTime, "TRANSFER");

            stats.put("rechargeTotal", rechargeTotal);
            stats.put("purchaseTotal", purchaseTotal);
            stats.put("refundTotal", refundTotal);
            stats.put("commissionTotal", commissionTotal);
            stats.put("withdrawTotal", withdrawTotal);
            stats.put("transferTotal", transferTotal);

            return Result.ok(stats);
        } catch (Exception e) {
            log.error("查询交易总额统计失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 导出财务日志
     */
    @PostMapping("/export")
    @ApiOperation("导出财务日志")
    @PreAuthorize("hasPermission(null ,'superadmin')")
//    @OperationLog(module = "财务日志管理", operationType = "导出", description = "导出财务日志", isLogin = false)
    @DataSource(DataSourceType.SLAVE)
    public void exportMoneyLog(@RequestBody MoneyLogQueryDTO queryDTO,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            List<MoneyLog> moneyLogs = moneyLogService.exportMoneyLog(queryDTO);

            // 转换为导出VO，处理枚举类型
            List<MoneyLogExportVO> exportData = moneyLogs.stream()
                    .map(this::convertToExportVO)
                    .collect(Collectors.toList());

            String fileName = "财务日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 使用ExcelUtils工具类导出
            ExcelUtils.exportExcel(response, exportData, fileName, "财务日志", MoneyLogExportVO.class);
            log.info("财务日志导出成功，共导出 {} 条记录", exportData.size());

        } catch (Exception e) {
            log.error("导出财务日志失败", e);
            try {
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }

    /**
     * 获取操作类型枚举
     */
    @GetMapping("/operation-types")
    @ApiOperation("获取操作类型枚举")
    @PreAuthorize("hasPermission(null ,'superadmin')")
    public Result<Map<String, String>> getOperationTypes() {
        try {
            Map<String, String> operationTypes = new HashMap<>();
            for (MoneyLog.OperationType type : MoneyLog.OperationType.values()) {
                operationTypes.put(type.getCode(), type.getDesc());
            }
            return Result.ok(operationTypes);
        } catch (Exception e) {
            log.error("获取操作类型枚举失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 获取状态枚举
     */
    @GetMapping("/status-types")
    @ApiOperation("获取状态枚举")
    @PreAuthorize("hasPermission(null ,'superadmin')")
    public Result<Map<Integer, String>> getStatusTypes() {
        try {
            Map<Integer, String> statusTypes = new HashMap<>();
            for (MoneyLog.Status status : MoneyLog.Status.values()) {
                statusTypes.put(status.getCode(), status.getDesc());
            }
            return Result.ok(statusTypes);
        } catch (Exception e) {
            log.error("获取状态枚举失败", e);
            return Result.fail(null);
        }
    }

    /**
     * 转换MoneyLog为导出VO，处理枚举类型
     */
    private MoneyLogExportVO convertToExportVO(MoneyLog moneyLog) {
        MoneyLogExportVO exportVO = new MoneyLogExportVO();
        exportVO.setLogId(moneyLog.getLogId());
        exportVO.setUserId(moneyLog.getUserId());
        exportVO.setUsername(moneyLog.getUsername());

        // 转换用户类型枚举
        exportVO.setUserTypeName(getUserTypeName(moneyLog.getUserType()));

        // 转换操作类型枚举
        exportVO.setOperationTypeName(getOperationTypeName(moneyLog.getOperationType()));

        exportVO.setAmount(moneyLog.getAmount());
        exportVO.setBalanceBefore(moneyLog.getBalanceBefore());
        exportVO.setBalanceAfter(moneyLog.getBalanceAfter());
        exportVO.setCurrency(moneyLog.getCurrency());
        exportVO.setRelatedOrderNo(moneyLog.getRelatedOrderNo());
        exportVO.setDescription(moneyLog.getDescription());
        exportVO.setRemark(moneyLog.getRemark());
        exportVO.setOperatorId(moneyLog.getOperatorId());
        exportVO.setOperatorName(moneyLog.getOperatorName());
        exportVO.setIpAddress(moneyLog.getIpAddress());

        // 转换状态枚举
        exportVO.setStatusName(getStatusName(moneyLog.getStatus()));

        exportVO.setCreatedAt(moneyLog.getCreatedAt());
        exportVO.setUpdatedAt(moneyLog.getUpdatedAt());

        return exportVO;
    }

    /**
     * 获取用户类型名称
     */
    private String getUserTypeName(Integer userType) {
        if (userType == null) return "未知";
        switch (userType) {
            case 1: return "超级管理员";
            case 2: return "管理员";
            case 3: return "采购员";
            case 4: return "商户";
            default: return "未知";
        }
    }

    /**
     * 获取操作类型名称
     */
    private String getOperationTypeName(String operationType) {
        if (operationType == null) return "未知";

        // 尝试从枚举中获取描述
        try {
            MoneyLog.OperationType type = MoneyLog.OperationType.valueOf(operationType);
            return type.getDesc();
        } catch (IllegalArgumentException e) {
            // 如果枚举中没有找到，返回原值
            return operationType;
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";

        // 尝试从枚举中获取描述
        try {
            String s = MoneyLog.Status.fromCode(status);
            return s;
        } catch (Exception e) {
            // 如果枚举中没有找到，返回默认值
            switch (status) {
                case 1: return "成功";
                case 2: return "失败";
                case 3: return "处理中";
                default: return "未知";
            }
        }
    }
}
