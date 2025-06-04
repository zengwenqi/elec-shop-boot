package elec.shop.controller;

import elec.shop.dto.UserDetailVO;
import elec.shop.service.SysUserService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public Result<UserDetailVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetailVO userDetail = userService.getUserDetail(username);
        return Result.ok(userDetail);
    }

    @ApiOperation("根据ID获取用户信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<UserDetailVO> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable("id") Long userId) {
        UserDetailVO userDetail = userService.getUserDetailById(userId);
        return Result.ok(userDetail);
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result<Void> updateUserStatus(
            @ApiParam(value = "用户ID", required = true) @RequestParam("userId") Long userId,
            @ApiParam(value = "状态：0-禁用 1-启用", required = true) @RequestParam("status") Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.ok();
    }
}
