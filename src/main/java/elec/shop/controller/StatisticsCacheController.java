package elec.shop.controller;

import elec.shop.job.StatisticsCacheTask;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "统计缓存管理接口")
@RestController
@RequestMapping("/statistics/cache")
@RequiredArgsConstructor
public class StatisticsCacheController {

    private final StatisticsCacheTask statisticsCacheTask;

    @ApiOperation("手动更新统计数据缓存")
    @PostMapping("/update")
    public Result<?> updateCache() {
        try {
            statisticsCacheTask.manualUpdateCache();
            return Result.ok("缓存更新成功");
        } catch (Exception e) {
            return Result.fail().message("缓存更新失败: " + e.getMessage());
        }
    }

    @ApiOperation("清除所有统计数据缓存")
    @PostMapping("/clear")
    public Result<?> clearCache() {
        try {
            statisticsCacheTask.clearAllCache();
            return Result.ok("缓存清除成功");
        } catch (Exception e) {
            return Result.fail().message("缓存清除失败: " + e.getMessage());
        }
    }
}
