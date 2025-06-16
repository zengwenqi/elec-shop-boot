package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.sys.dto.AssignRoleDTO;
import elec.shop.pojo.sys.dto.UserDetailVO;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/users")
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
//    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<UserDetailVO> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable("id") Long userId) {
        UserDetailVO userDetail = userService.getUserDetailById(userId);
        return Result.ok(userDetail);
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/status")
//    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result<Void> updateUserStatus(
            @ApiParam(value = "用户ID", required = true) @RequestParam("userId") Long userId,
            @ApiParam(value = "状态：0-禁用 1-启用", required = true) @RequestParam("status") Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.ok();
    }

    @ApiOperation("分页查询用户列表")
    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<IPage<UserDetailVO>> getUserList(
            @ApiParam(value = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword) {
        IPage<UserDetailVO> userList = userService.getUserList(pageNum, pageSize, keyword);
        return Result.ok(userList);
    }

    @ApiOperation("分配用户角色")
    @PostMapping("/assign-roles")
//    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result<Void> assignUserRoles(@Validated @RequestBody AssignRoleDTO assignRoleDTO) {
        userService.assignUserRoles(assignRoleDTO);
        return Result.ok();
    }
}
