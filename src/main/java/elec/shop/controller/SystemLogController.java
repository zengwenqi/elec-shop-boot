package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.pojo.sys.dto.SystemLogQueryDTO;
import elec.shop.pojo.sys.vo.SystemLogVO;
import elec.shop.service.sys.SystemLogService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Api(tags = "系统日志管理")
@RestController
@RequestMapping("/system-log")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    @ApiOperation("分页查询系统日志")
    @GetMapping("/system-logs")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<IPage<SystemLogVO>> getSystemLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status) {

        SystemLogQueryDTO queryDTO = new SystemLogQueryDTO();
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setLogType(logType);
        queryDTO.setLogLevel(logLevel);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setKeyword(keyword);
        queryDTO.setOperator(operator);
        queryDTO.setIp(ip);
        queryDTO.setStatus(status);

        IPage<SystemLogVO> systemLogs = systemLogService.getSystemLogs(queryDTO);
        return Result.ok(systemLogs);
    }

    @ApiOperation("获取日志详情")
    @GetMapping("/system-logs/{logId}")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<SystemLogVO> getLogDetail(@PathVariable Long logId) {
        SystemLogVO logDetail = systemLogService.getLogDetail(logId);
        return Result.ok(logDetail);
    }

    @ApiOperation("导出系统日志")
    @PostMapping("/system-logs/export")
    @DataSource(value = DataSourceType.SLAVE)
    public void exportSystemLogs(@RequestBody SystemLogQueryDTO queryDTO, HttpServletResponse response) {
        systemLogService.exportSystemLogs(queryDTO, response);
    }

    @ApiOperation("清空系统日志")
    @DeleteMapping("/system-logs")
    public Result<Boolean> clearSystemLogs(@RequestParam(required = false) String logType) {
        Boolean result = systemLogService.clearSystemLogs(logType);
        return Result.ok(result);
    }

    @ApiOperation("获取日志统计信息")
    @GetMapping("/system-logs/statistics")
    @DataSource(value = DataSourceType.SLAVE)
    public Result<Object> getLogStatistics() {
        Object logStatistics = systemLogService.getLogStatistics();
        return Result.ok(logStatistics);
    }

}
