package elec.shop.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.pojo.sys.SysLoginLog;
import elec.shop.pojo.sys.SysOperationLog;
import elec.shop.pojo.sys.dto.SystemLogQueryDTO;
import elec.shop.pojo.sys.vo.SystemLogVO;
import elec.shop.service.sys.SysLoginLogService;
import elec.shop.service.sys.SysOperationLogService;
import elec.shop.service.sys.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import elec.shop.pojo.sys.vo.SystemLogExportVO;
import elec.shop.utils.ExcelUtils;

/**
 * 系统日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SysLoginLogService loginLogService;
    private final SysOperationLogService operationLogService;

    @Override
    public IPage<SystemLogVO> getSystemLogs(SystemLogQueryDTO queryDTO) {
        List<SystemLogVO> allLogs = new ArrayList<>();

        // 根据日志类型查询不同的日志
        if (queryDTO.getLogType() == null || "system".equals(queryDTO.getLogType()) || "security".equals(queryDTO.getLogType())) {
            // 查询登录日志
            List<SysLoginLog> loginLogs = getLoginLogs(queryDTO);
            allLogs.addAll(convertLoginLogsToVO(loginLogs));
        }

        if (queryDTO.getLogType() == null || "operation".equals(queryDTO.getLogType()) || "error".equals(queryDTO.getLogType())) {
            // 查询操作日志
            List<SysOperationLog> operationLogs = getOperationLogs(queryDTO);
            allLogs.addAll(convertOperationLogsToVO(operationLogs));
        }

        // 按时间倒序排序
        allLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // 手动分页
        int total = allLogs.size();
        int start = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        int end = Math.min(start + queryDTO.getPageSize(), total);

        List<SystemLogVO> pageData = start < total ? allLogs.subList(start, end) : new ArrayList<>();

        Page<SystemLogVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), total);
        page.setRecords(pageData);

        return page;
    }

    @Override
    public SystemLogVO getLogDetail(Long logId) {
        // 先从登录日志查找
        SysLoginLog loginLog = loginLogService.getById(logId);
        if (loginLog != null) {
            return convertLoginLogToVO(loginLog);
        }

        // 再从操作日志查找
        SysOperationLog operationLog = operationLogService.getById(logId);
        if (operationLog != null) {
            return convertOperationLogToVO(operationLog);
        }

        return null;
    }

    @Override
    public void exportSystemLogs(SystemLogQueryDTO queryDTO, HttpServletResponse response) {
        List<SystemLogVO> allLogs = new ArrayList<>();

        // 根据日志类型查询不同的日志
        if (queryDTO.getLogType() == null || "system".equals(queryDTO.getLogType()) || "security".equals(queryDTO.getLogType())) {
            // 查询登录日志
            List<SysLoginLog> loginLogs = getLoginLogs(queryDTO);
            allLogs.addAll(convertLoginLogsToVO(loginLogs));
        }

        if (queryDTO.getLogType() == null || "operation".equals(queryDTO.getLogType()) || "error".equals(queryDTO.getLogType())) {
            // 查询操作日志
            List<SysOperationLog> operationLogs = getOperationLogs(queryDTO);
            allLogs.addAll(convertOperationLogsToVO(operationLogs));
        }

        // 按时间倒序排序
        allLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // 转换为导出VO
        List<SystemLogExportVO> exportData = allLogs.stream()
                .map(this::convertToExportVO)
                .collect(Collectors.toList());

        // 生成文件名
        String fileName = "系统日志_" + new Date().getTime();

        // 导出Excel
        ExcelUtils.exportExcel(response, exportData, fileName, "系统日志", SystemLogExportVO.class);
    }

    @Override
    public Boolean clearSystemLogs(String logType) {
        try {
            if (logType == null || "system".equals(logType) || "security".equals(logType)) {
                loginLogService.remove(new LambdaQueryWrapper<>());
            }
            if (logType == null || "operation".equals(logType) || "error".equals(logType)) {
                operationLogService.remove(new LambdaQueryWrapper<>());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getLogStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 登录日志统计
        long loginLogCount = loginLogService.count();
        long loginSuccessCount = loginLogService.count(new LambdaQueryWrapper<SysLoginLog>().eq(SysLoginLog::getStatus, 1));
        long loginFailCount = loginLogCount - loginSuccessCount;

        // 操作日志统计
        long operationLogCount = operationLogService.count();
        long operationSuccessCount = operationLogService.count(new LambdaQueryWrapper<SysOperationLog>().eq(SysOperationLog::getStatus, 1));
        long operationFailCount = operationLogCount - operationSuccessCount;

        statistics.put("totalLogs", loginLogCount + operationLogCount);
        statistics.put("loginLogs", loginLogCount);
        statistics.put("operationLogs", operationLogCount);
        statistics.put("successCount", loginSuccessCount + operationSuccessCount);
        statistics.put("failCount", loginFailCount + operationFailCount);

        return statistics;
    }

    private List<SysLoginLog> getLoginLogs(SystemLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(SysLoginLog::getUsername, queryDTO.getKeyword())
                    .or().like(SysLoginLog::getIp, queryDTO.getKeyword())
                    .or().like(SysLoginLog::getLocation, queryDTO.getKeyword()));
        }

        if (StringUtils.hasText(queryDTO.getOperator())) {
            wrapper.like(SysLoginLog::getUsername, queryDTO.getOperator());
        }

        if (StringUtils.hasText(queryDTO.getIp())) {
            wrapper.like(SysLoginLog::getIp, queryDTO.getIp());
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysLoginLog::getStatus, queryDTO.getStatus());
        }

        if (queryDTO.getStartTime() != null) {
            wrapper.ge(SysLoginLog::getLoginTime, queryDTO.getStartTime());
        }

        if (queryDTO.getEndTime() != null) {
            wrapper.le(SysLoginLog::getLoginTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(SysLoginLog::getLoginTime);

        return loginLogService.list(wrapper);
    }

    private List<SysOperationLog> getOperationLogs(SystemLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(SysOperationLog::getUsername, queryDTO.getKeyword())
                    .or().like(SysOperationLog::getOperationType, queryDTO.getKeyword())
                    .or().like(SysOperationLog::getMethod, queryDTO.getKeyword())
                    .or().like(SysOperationLog::getIp, queryDTO.getKeyword()));
        }

        if (StringUtils.hasText(queryDTO.getOperator())) {
            wrapper.like(SysOperationLog::getUsername, queryDTO.getOperator());
        }

        if (StringUtils.hasText(queryDTO.getIp())) {
            wrapper.like(SysOperationLog::getIp, queryDTO.getIp());
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysOperationLog::getStatus, queryDTO.getStatus());
        }

        if (queryDTO.getStartTime() != null) {
            wrapper.ge(SysOperationLog::getCreatedAt, queryDTO.getStartTime());
        }

        if (queryDTO.getEndTime() != null) {
            wrapper.le(SysOperationLog::getCreatedAt, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(SysOperationLog::getCreatedAt);

        return operationLogService.list(wrapper);
    }

    private List<SystemLogVO> convertLoginLogsToVO(List<SysLoginLog> loginLogs) {
        return loginLogs.stream().map(this::convertLoginLogToVO).collect(Collectors.toList());
    }

    private List<SystemLogVO> convertOperationLogsToVO(List<SysOperationLog> operationLogs) {
        return operationLogs.stream().map(this::convertOperationLogToVO).collect(Collectors.toList());
    }

    private SystemLogVO convertLoginLogToVO(SysLoginLog loginLog) {
        SystemLogVO vo = new SystemLogVO();
        vo.setLogId(loginLog.getLogId().toString());
        vo.setTimestamp(loginLog.getLoginTime());
        vo.setType(loginLog.getStatus() == 1 ? "system" : "security");
        vo.setLevel(loginLog.getStatus() == 1 ? "info" : "warning");
        vo.setContent(loginLog.getStatus() == 1 ? "用户登录成功" : "用户登录失败: " + loginLog.getMsg());
        vo.setOperator(loginLog.getUsername());
        vo.setIp(loginLog.getIp());
        vo.setUserAgent(loginLog.getBrowser() + " " + loginLog.getOs());
        vo.setLocation(loginLog.getLocation());
        vo.setBrowser(loginLog.getBrowser());
        vo.setOs(loginLog.getOs());
        vo.setStatus(loginLog.getStatus());
        vo.setTime(loginLog.getTime());
        if (loginLog.getStatus() == 0) {
            vo.setStack(loginLog.getMsg());
        }
        return vo;
    }

    private SystemLogVO convertOperationLogToVO(SysOperationLog operationLog) {
        SystemLogVO vo = new SystemLogVO();
        vo.setLogId(operationLog.getLogId().toString());
        vo.setTimestamp(operationLog.getCreatedAt());
        vo.setType(operationLog.getStatus() == 1 ? "operation" : "error");
        vo.setLevel(operationLog.getStatus() == 1 ? "info" : "error");
        vo.setContent(operationLog.getOperationType() + ": " + operationLog.getMethod());
        vo.setOperator(operationLog.getUsername());
        vo.setIp(operationLog.getIp());
        vo.setUserAgent(operationLog.getBrowser() + " " + operationLog.getOs());
        vo.setLocation(operationLog.getLocation());
        vo.setBrowser(operationLog.getBrowser());
        vo.setOs(operationLog.getOs());
        vo.setStatus(operationLog.getStatus());
        vo.setTime(operationLog.getTime());
        vo.setMethod(operationLog.getMethod());
        vo.setParams(operationLog.getParams());
        if (operationLog.getStatus() == 0) {
            vo.setStack(operationLog.getErrorMsg());
        }
        return vo;
    }

    private SystemLogExportVO convertToExportVO(SystemLogVO vo) {
        SystemLogExportVO exportVO = new SystemLogExportVO();
        exportVO.setLogId(vo.getLogId());
        exportVO.setTimestamp(vo.getTimestamp());
        exportVO.setTypeName(getLogTypeName(vo.getType()));
        exportVO.setLevelName(getLogLevelName(vo.getLevel()));
        exportVO.setContent(vo.getContent());
        exportVO.setOperator(vo.getOperator());
        exportVO.setIp(vo.getIp());
        exportVO.setLocation(vo.getLocation());
        exportVO.setBrowser(vo.getBrowser());
        exportVO.setOs(vo.getOs());
        exportVO.setStatusName(vo.getStatus() == 1 ? "成功" : "失败");
        exportVO.setTime(vo.getTime());
        exportVO.setMethod(vo.getMethod());
        exportVO.setParams(vo.getParams());
        exportVO.setStack(vo.getStack());
        return exportVO;
    }

    private String getLogTypeName(String type) {
        switch (type) {
            case "system": return "系统日志";
            case "operation": return "操作日志";
            case "security": return "安全日志";
            case "error": return "错误日志";
            default: return "其他日志";
        }
    }

    private String getLogLevelName(String level) {
        switch (level) {
            case "info": return "信息";
            case "warning": return "警告";
            case "error": return "错误";
            case "critical": return "严重";
            default: return "未知";
        }
    }
}
