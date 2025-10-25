package elec.shop.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import elec.shop.annotation.DataSource;
import elec.shop.config.DataSourceType;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.announcement.CrossBorderService;
import elec.shop.pojo.announcement.dto.CrossBorderDTO;
import elec.shop.pojo.announcement.vo.CrossBorderVO;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.announcement.CrossBorderServiceService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.MinioUtil;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "跨境服务超市管理")
@RestController
@RequestMapping("/cross-border")
@RequiredArgsConstructor
public class BorderServiceController {

    private final CrossBorderServiceService crossBorderServiceService;
    private final MinioUtil minioUtil;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/list")
    @ApiOperation("查询所有跨境服务超市")
    @DataSource(DataSourceType.SLAVE)
    public Result<Object> list() {

        SysUser sysUser = sysUserMapper.selectById(AllContextUtils.getLoginSysUser().getUserId());
        List<CrossBorderService> list;
        if (sysUser.getUserType() == 1){
            // 1. 查询原始数据
            list = crossBorderServiceService.list();
        } else {
            // 1. 查询原始数据
            list = crossBorderServiceService.list(
                    new LambdaQueryWrapper<CrossBorderService>()
                            .eq(CrossBorderService::getStatus, 1)
            );
        }

        // 2. 转换为VO对象（修正复制逻辑）
        List<CrossBorderVO> crossBorderVOList = list.stream()
                .map(service -> {
                    CrossBorderVO vo = new CrossBorderVO();
                    BeanUtil.copyProperties(service, vo); // 源对象 -> 目标对象
                    return vo;
                })
                .collect(Collectors.toList());

        // 3. 处理URL（如果VO列表不为空）
        crossBorderVOList.forEach(e -> {
            try {
                if (e.getServiceIcon() != null) {
                    e.setServiceIcon(minioUtil.getPreviewUrl(e.getServiceIcon()));
                }
                if (e.getServiceImage() != null) {
                    e.setServiceImage(minioUtil.getPreviewUrl(e.getServiceImage()));
                }
            } catch (Exception ex) {
                Result.fail().message("处理URL时出错: " + ex.getMessage());
            }
        });

        return Result.ok(crossBorderVOList);
    }

    @PostMapping("/add")
    @ApiOperation("新增跨境服务超市")
    @PreAuthorize("hasPermission(null ,'superadmin')")
    @DataSource(DataSourceType.MASTER)
    public Result<Object> add(@RequestBody CrossBorderDTO crossBorderDTO) {
        CrossBorderService crossBorderService = new CrossBorderService();
        BeanUtil.copyProperties(crossBorderDTO, crossBorderService);
        boolean insert = crossBorderServiceService.save(crossBorderService);
        if (insert) return Result.ok();
        return Result.fail().message("改跨境服务超市编码已存在");
    }

    @PostMapping("/edit")
    @ApiOperation("编辑跨境服务超市")
    @PreAuthorize("hasPermission(null ,'superadmin')")
    @DataSource(DataSourceType.MASTER)
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
    @DataSource(DataSourceType.MASTER)
    public Result<Object> delete(@RequestParam String serviceCode) {
        boolean remove = crossBorderServiceService.remove(
                new LambdaQueryWrapper<CrossBorderService>()
                        .eq(CrossBorderService::getServiceCode, serviceCode)
        );
        if (remove) return Result.ok();
        return Result.fail().message("删除失败");
    }
}
