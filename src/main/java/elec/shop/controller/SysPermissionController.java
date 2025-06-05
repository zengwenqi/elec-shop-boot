package elec.shop.controller;

import elec.shop.pojo.sys.SysPermission;
import elec.shop.service.sys.SysPermissionService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "权限管理")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @ApiOperation("添加权限")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:permission:add')")
    public Result<Void> addPermission(@RequestBody SysPermission permission) {
        permissionService.savePermission(permission);
        return Result.ok();
    }

    @ApiOperation("更新权限")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:permission:update')")
    public Result<Void> updatePermission(@RequestBody SysPermission permission) {
        permissionService.updatePermission(permission);
        return Result.ok();
    }

    @ApiOperation("删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:delete')")
    public Result<Void> deletePermission(
            @ApiParam(value = "权限ID", required = true) @PathVariable("id") Long permissionId) {
        permissionService.deletePermission(permissionId);
        return Result.ok();
    }

    @ApiOperation("获取当前用户权限")
    @GetMapping("/user")
    public Result<String[]> getCurrentUserPermissions() {
        // TODO: 从SecurityContext中获取当前用户ID
        return Result.ok(new String[0]);
    }
}
