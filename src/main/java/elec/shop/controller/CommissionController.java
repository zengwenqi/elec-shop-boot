package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.annotation.DataSource;
import elec.shop.annotation.OperationLog;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.balance.CommissionRecord;
import elec.shop.pojo.balance.GlobalCommissionConfig;
import elec.shop.pojo.balance.UserCommissionConfig;
import elec.shop.pojo.balance.dto.CommissionQueryDTO;
import elec.shop.pojo.balance.dto.GlobalCommissionConfigDTO;
import elec.shop.pojo.balance.dto.UserCommissionConfigDTO;
import elec.shop.pojo.balance.vo.CommissionRecordVO;
import elec.shop.pojo.balance.vo.GlobalCommissionConfigVO;
import elec.shop.pojo.balance.vo.UserCommissionConfigVO;
import elec.shop.service.balance.CommissionRecordService;
import elec.shop.service.balance.GlobalCommissionConfigService;
import elec.shop.service.balance.UserCommissionConfigService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Api(tags = "佣金管理")
@RestController
@RequestMapping("/commission")
@RequiredArgsConstructor
@Slf4j
public class CommissionController {

    private final GlobalCommissionConfigService globalCommissionConfigService;
    private final UserCommissionConfigService userCommissionConfigService;
    private final CommissionRecordService commissionRecordService;

    // ==================== 全局佣金配置管理 ====================

    @GetMapping("/global/page")
    @ApiOperation("分页查询全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<IPage<GlobalCommissionConfigVO>> getGlobalConfigPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("用户类型") @RequestParam(required = false) Integer userType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        IPage<GlobalCommissionConfigVO> page = globalCommissionConfigService.getGlobalConfigPage(
                pageNum, pageSize, userType, status, keyword);
        return Result.ok(page);
    }

    @GetMapping("/global/list")
    @ApiOperation("查询全局佣金配置列表")
//    @PreAuthorize("hasAuthority('commission:global:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<List<GlobalCommissionConfigVO>> getGlobalConfigList(
            @ApiParam("用户类型") @RequestParam(required = false) Integer userType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        List<GlobalCommissionConfigVO> list = globalCommissionConfigService.getGlobalConfigList(
                userType, status, keyword);
        return Result.ok(list);
    }

    @GetMapping("/global/{configId}")
    @ApiOperation("根据ID查询全局佣金配置详情")
//    @PreAuthorize("hasAuthority('commission:global:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<GlobalCommissionConfigVO> getGlobalConfigById(
            @ApiParam("配置ID") @PathVariable Long configId) {
        GlobalCommissionConfigVO config = globalCommissionConfigService.getGlobalConfigById(configId);
        return Result.ok(config);
    }

    @PostMapping("/global")
    @ApiOperation("创建全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:create')")
    @OperationLog(module = "佣金管理", operationType = "创建全局配置", description = "创建全局佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> createGlobalConfig(
            @ApiParam("全局佣金配置信息") @RequestBody @Validated GlobalCommissionConfigDTO configDTO) {
        Boolean result = globalCommissionConfigService.createGlobalConfig(configDTO);
        return Result.ok(result);
    }

    @PutMapping("/global/{configId}")
    @ApiOperation("更新全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:update')")
    @OperationLog(module = "佣金管理", operationType = "更新全局配置", description = "更新全局佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> updateGlobalConfig(
            @ApiParam("配置ID") @PathVariable Long configId,
            @ApiParam("全局佣金配置信息") @RequestBody @Validated GlobalCommissionConfigDTO configDTO) {
        Boolean result = globalCommissionConfigService.updateGlobalConfig(configId, configDTO);
        return Result.ok(result);
    }

    @DeleteMapping("/global/{configId}")
    @ApiOperation("删除全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:delete')")
    @OperationLog(module = "佣金管理", operationType = "删除全局配置", description = "删除全局佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> deleteGlobalConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        Boolean result = globalCommissionConfigService.deleteGlobalConfig(configId);
        return Result.ok(result);
    }

    @DeleteMapping("/global/batch")
    @ApiOperation("批量删除全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:delete')")
    @OperationLog(module = "佣金管理", operationType = "批量删除全局配置", description = "批量删除全局佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> batchDeleteGlobalConfig(
            @ApiParam("配置ID列表") @RequestBody List<Long> configIds) {
        Boolean result = globalCommissionConfigService.batchDeleteGlobalConfig(configIds);
        return Result.ok(result);
    }

    @PutMapping("/global/{configId}/enable")
    @ApiOperation("启用全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:update')")
    @OperationLog(module = "佣金管理", operationType = "启用全局配置", description = "启用全局佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> enableGlobalConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        Boolean result = globalCommissionConfigService.updateGlobalConfigStatus(configId, 1);
        return Result.ok(result);
    }

    @PutMapping("/global/{configId}/disable")
    @ApiOperation("禁用全局佣金配置")
//    @PreAuthorize("hasAuthority('commission:global:update')")
    @OperationLog(module = "佣金管理", operationType = "禁用全局配置", description = "禁用全局佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> disableGlobalConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        Boolean result = globalCommissionConfigService.updateGlobalConfigStatus(configId, 0);
        return Result.ok(result);
    }

    // ==================== 用户佣金配置管理 ====================

    @GetMapping("/user/page")
    @ApiOperation("分页查询用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<IPage<UserCommissionConfigVO>> getUserConfigPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("用户类型") @RequestParam(required = false) Integer userType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        IPage<UserCommissionConfigVO> page = userCommissionConfigService.getUserConfigPage(
                pageNum, pageSize, userType, status, keyword);
        return Result.ok(page);
    }

    @GetMapping("/user/list")
    @ApiOperation("查询用户佣金配置列表")
//    @PreAuthorize("hasAuthority('commission:user:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<List<UserCommissionConfigVO>> getUserConfigList(
            @ApiParam("用户类型") @RequestParam(required = false) Integer userType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        List<UserCommissionConfigVO> list = userCommissionConfigService.getUserConfigList(
                userType, status, keyword);
        return Result.ok(list);
    }

    @GetMapping("/user/{configId}")
    @ApiOperation("根据ID查询用户佣金配置详情")
//    @PreAuthorize("hasAuthority('commission:user:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<UserCommissionConfigVO> getUserConfigById(
            @ApiParam("配置ID") @PathVariable Long configId) {
        UserCommissionConfigVO config = userCommissionConfigService.getUserConfigById(configId);
        return Result.ok(config);
    }

    @GetMapping("/user/by-user/{userId}")
    @ApiOperation("根据用户ID查询佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<UserCommissionConfigVO> getUserConfigByUserId(
            @ApiParam("用户ID") @PathVariable Long userId) {
        UserCommissionConfigVO config = userCommissionConfigService.getUserConfigByUserId(userId);
        return Result.ok(config);
    }

    @PostMapping("/user")
    @ApiOperation("创建用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:create')")
    @OperationLog(module = "佣金管理", operationType = "创建用户配置", description = "创建用户佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> createUserConfig(
            @ApiParam("用户佣金配置信息") @RequestBody @Validated UserCommissionConfigDTO configDTO) {
        Boolean result = userCommissionConfigService.createUserConfig(configDTO);
        return Result.ok(result);
    }

    @PutMapping("/user/{configId}")
    @ApiOperation("更新用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:update')")
    @OperationLog(module = "佣金管理", operationType = "更新用户配置", description = "更新用户佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> updateUserConfig(
            @ApiParam("配置ID") @PathVariable Long configId,
            @ApiParam("用户佣金配置信息") @RequestBody @Validated UserCommissionConfigDTO configDTO) {
        Boolean result = userCommissionConfigService.updateUserConfig(configId, configDTO);
        return Result.ok(result);
    }

    @DeleteMapping("/user/{configId}")
    @ApiOperation("删除用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:delete')")
    @OperationLog(module = "佣金管理", operationType = "删除用户配置", description = "删除用户佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> deleteUserConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        Boolean result = userCommissionConfigService.deleteUserConfig(configId);
        return Result.ok(result);
    }

    @DeleteMapping("/user/batch")
    @ApiOperation("批量删除用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:delete')")
    @OperationLog(module = "佣金管理", operationType = "批量删除用户配置", description = "批量删除用户佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> batchDeleteUserConfig(
            @ApiParam("配置ID列表") @RequestBody List<Long> configIds) {
        Boolean result = userCommissionConfigService.batchDeleteUserConfig(configIds);
        return Result.ok(result);
    }

    @PutMapping("/user/{configId}/enable")
    @ApiOperation("启用用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:update')")
    @OperationLog(module = "佣金管理", operationType = "启用用户配置", description = "启用用户佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> enableUserConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        Boolean result = userCommissionConfigService.updateUserConfigStatus(configId, 1);
        return Result.ok(result);
    }

    @PutMapping("/user/{configId}/disable")
    @ApiOperation("禁用用户佣金配置")
//    @PreAuthorize("hasAuthority('commission:user:update')")
    @OperationLog(module = "佣金管理", operationType = "禁用用户配置", description = "禁用用户佣金配置")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> disableUserConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        Boolean result = userCommissionConfigService.updateUserConfigStatus(configId, 0);
        return Result.ok(result);
    }

    // ==================== 佣金记录管理 ====================

    @PostMapping("/record/page")
    @ApiOperation("分页查询佣金记录")
//    @PreAuthorize("hasAuthority('commission:record:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<IPage<CommissionRecordVO>> getCommissionRecordPage(
            @ApiParam("查询参数") @RequestBody CommissionQueryDTO queryDTO) {
        IPage<CommissionRecordVO> page = commissionRecordService.getCommissionRecordPage(queryDTO);
        return Result.ok(page);
    }

    @PostMapping("/record/list")
    @ApiOperation("查询佣金记录列表")
//    @PreAuthorize("hasAuthority('commission:record:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<List<CommissionRecordVO>> getCommissionRecordList(
            @ApiParam("查询参数") @RequestBody CommissionQueryDTO queryDTO) {
        List<CommissionRecordVO> list = commissionRecordService.getCommissionRecordList(queryDTO);
        return Result.ok(list);
    }

    @GetMapping("/record/{recordId}")
    @ApiOperation("根据ID查询佣金记录详情")
//    @PreAuthorize("hasAuthority('commission:record:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<CommissionRecordVO> getCommissionRecordById(
            @ApiParam("记录ID") @PathVariable Long recordId) {
        CommissionRecordVO record = commissionRecordService.getCommissionRecordById(recordId);
        return Result.ok(record);
    }

    @GetMapping("/record/order/{orderId}")
    @ApiOperation("根据订单ID查询佣金记录")
//    @PreAuthorize("hasAuthority('commission:record:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<List<CommissionRecord>> getCommissionRecordByOrderId(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        List<CommissionRecord> records = commissionRecordService.getCommissionRecordByOrderId(orderId);
        return Result.ok(records);
    }

    @GetMapping("/record/user/{userId}")
    @ApiOperation("根据用户ID查询佣金记录")
//    @PreAuthorize("hasAuthority('commission:record:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<List<CommissionRecordVO>> getCommissionRecordByUserId(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("佣金状态") @RequestParam(required = false) Integer commissionStatus) {
        List<CommissionRecordVO> records = commissionRecordService.getCommissionRecordByUserId(userId, commissionStatus);
        return Result.ok(records);
    }

    @PostMapping("/record/create")
    @ApiOperation("创建佣金记录")
//    @PreAuthorize("hasAuthority('commission:record:create')")
    @OperationLog(module = "佣金管理", operationType = "创建佣金记录", description = "创建佣金记录")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> createCommissionRecord(
            @ApiParam("订单ID") @RequestParam Long orderId,
            @ApiParam("订单号") @RequestParam String orderNo,
            @ApiParam("订单金额") @RequestParam BigDecimal orderAmount,
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("用户类型") @RequestParam Integer userType) {
        Boolean result = commissionRecordService.createCommissionRecord(orderId, orderNo, orderAmount, userId, userType);
        return Result.ok(result);
    }

    @PostMapping("/record/batch-create")
    @ApiOperation("批量创建佣金记录")
//    @PreAuthorize("hasAuthority('commission:record:create')")
    @OperationLog(module = "佣金管理", operationType = "批量创建佣金记录", description = "批量创建佣金记录")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> batchCreateCommissionRecord(
            @ApiParam("订单ID列表") @RequestBody List<Long> orderIds) {
        Boolean result = commissionRecordService.batchCreateCommissionRecord(orderIds);
        return Result.ok(result);
    }

    @PutMapping("/record/{recordId}/payout")
    @ApiOperation("发放佣金")
//    @PreAuthorize("hasAuthority('commission:record:payout')")
    @OperationLog(module = "佣金管理", operationType = "发放佣金", description = "发放佣金")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> payoutCommission(
            @ApiParam("记录ID") @PathVariable Long recordId) {
        Boolean result = commissionRecordService.payoutCommission(recordId);
        return Result.ok(result);
    }

    @PutMapping("/record/batch-payout")
    @ApiOperation("批量发放佣金")
//    @PreAuthorize("hasAuthority('commission:record:payout')")
    @OperationLog(module = "佣金管理", operationType = "批量发放佣金", description = "批量发放佣金")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> batchPayoutCommission(
            @ApiParam("记录ID列表") @RequestBody List<Long> recordIds) {
        Boolean result = commissionRecordService.batchPayoutCommission(recordIds);
        return Result.ok(result);
    }

    @PutMapping("/record/{recordId}/cancel")
    @ApiOperation("取消佣金")
//    @PreAuthorize("hasAuthority('commission:record:cancel')")
    @OperationLog(module = "佣金管理", operationType = "取消佣金", description = "取消佣金")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> cancelCommission(
            @ApiParam("记录ID") @PathVariable Long recordId,
            @ApiParam("取消原因") @RequestParam(required = false) String reason) {
        Boolean result = commissionRecordService.cancelCommission(recordId, reason);
        return Result.ok(result);
    }

    @PutMapping("/record/batch-cancel")
    @ApiOperation("批量取消佣金")
//    @PreAuthorize("hasAuthority('commission:record:cancel')")
    @OperationLog(module = "佣金管理", operationType = "批量取消佣金", description = "批量取消佣金")
    @DataSource(DataSourceType.MASTER)
    public Result<Boolean> batchCancelCommission(
            @ApiParam("记录ID列表") @RequestBody List<Long> recordIds,
            @ApiParam("取消原因") @RequestParam(required = false) String reason) {
        Boolean result = commissionRecordService.batchCancelCommission(recordIds, reason);
        return Result.ok(result);
    }

    // ==================== 统计查询接口 ====================

    @GetMapping("/statistics/user/{userId}/sum")
    @ApiOperation("统计用户佣金总额")
//    @PreAuthorize("hasAuthority('commission:statistics:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<BigDecimal> sumCommissionByUserId(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("佣金状态") @RequestParam(required = false) Integer commissionStatus,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime) {
        Date startDate = parseDate(startTime);
        Date endDate = parseDate(endTime);
        BigDecimal sum = commissionRecordService.sumCommissionByUserId(userId, commissionStatus, startDate, endDate);
        return Result.ok(sum);
    }

    @GetMapping("/statistics/count")
    @ApiOperation("统计佣金记录数量")
//    @PreAuthorize("hasAuthority('commission:statistics:query')")
    @DataSource(DataSourceType.SLAVE)
    public Result<Long> countCommissionRecord(
            @ApiParam("用户类型") @RequestParam(required = false) Integer userType,
            @ApiParam("佣金状态") @RequestParam(required = false) Integer commissionStatus,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime) {
        Date startDate = parseDate(startTime);
        Date endDate = parseDate(endTime);
        Long count = commissionRecordService.countCommissionRecord(userType, commissionStatus, startDate, endDate);
        return Result.ok(count);
    }

    @GetMapping("/calculate")
    @ApiOperation("计算订单佣金金额")
//    @PreAuthorize("hasAuthority('commission:calculate')")
    @DataSource(DataSourceType.SLAVE)
    public Result<BigDecimal> calculateCommissionAmount(
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("用户类型") @RequestParam Integer userType,
            @ApiParam("订单金额") @RequestParam BigDecimal orderAmount) {
        BigDecimal commission = commissionRecordService.calculateCommissionAmount(userId, userType, orderAmount);
        return Result.ok(commission);
    }

    /**
     * 解析日期字符串
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(dateStr);
            } catch (ParseException ex) {
                log.warn("日期格式解析失败: {}", dateStr);
                return null;
            }
        }
    }
}
