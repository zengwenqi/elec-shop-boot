package elec.shop.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import elec.shop.pojo.announcement.CrossBorderService;
import elec.shop.pojo.announcement.dto.CrossBorderDTO;
import elec.shop.pojo.announcement.vo.CrossBorderVO;
import elec.shop.service.announcement.CrossBorderServiceService;
import elec.shop.utils.MinioUtil;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "跨境服务超市管理")
@RestController
@RequestMapping("/cross-border")
@RequiredArgsConstructor
public class BorderServiceController {

    private final CrossBorderServiceService crossBorderServiceService;
    private final MinioUtil minioUtil;

    @GetMapping("/list")
    @ApiOperation("查询所有跨境服务超市")
    public Result<Object> list() {
        List<CrossBorderService> list = crossBorderServiceService.list();
        List<CrossBorderVO> crossBorderVOList = new ArrayList<>();
        list.forEach(e->{
            CrossBorderVO crossBorderVO = new CrossBorderVO();
            e.setServiceIcon(minioUtil.getPreviewUrl(e.getServiceIcon()));
            e.setServiceImage(minioUtil.getPreviewUrl(e.getServiceImage()));
            BeanUtil.copyProperties(list, crossBorderVO);
            crossBorderVOList.add(crossBorderVO);
        });
        return Result.ok(crossBorderVOList);
    }

    @PostMapping("/edit")
    @ApiOperation("编辑跨境服务超市")
    @PreAuthorize("hasPermission(null ,'superadmin')")
    public Result<Object> edit(@RequestBody CrossBorderDTO crossBorderDTO) {
        CrossBorderService crossBorderService = new CrossBorderService();
        BeanUtil.copyProperties(crossBorderDTO, crossBorderService);
        boolean update = crossBorderServiceService.update(
                crossBorderService,
                new LambdaQueryWrapper<CrossBorderService>()
                        .eq(CrossBorderService::getServiceCode, crossBorderDTO.getServiceCode())
        );
        if (update) return Result.ok();
        return Result.fail().message("更新失败");
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除跨境服务超市")
    @PreAuthorize("hasPermission(null ,'superadmin')")
    public Result<Object> delete(@RequestParam String serviceCode) {
        boolean remove = crossBorderServiceService.remove(
                new LambdaQueryWrapper<CrossBorderService>()
                        .eq(CrossBorderService::getServiceCode, serviceCode)
        );
        if (remove) return Result.ok();
        return Result.fail().message("删除失败");
    }
}
