package elec.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.exception.BusinessException;
import elec.shop.pojo.sys.SysUser;
import elec.shop.pojo.sys.dto.AssignRoleDTO;
import elec.shop.pojo.sys.dto.UpdatePasswordDTO;
import elec.shop.pojo.sys.dto.UserDetailVO;
import elec.shop.pojo.sys.dto.UpdateProfileDTO;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import elec.shop.utils.RsaDecryptUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @PreAuthorize("hasPermission(null, 'superadmin:role:query')")
    public Result<UserDetailVO> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable("id") Long userId) {
        UserDetailVO userDetail = userService.getUserDetailById(userId);
        return Result.ok(userDetail);
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/status")
    @PreAuthorize("hasPermission(null, 'superadmin:role:update')")
    public Result<Void> updateUserStatus(
            @ApiParam(value = "用户ID", required = true) @RequestParam("userId") Long userId,
            @ApiParam(value = "状态：0-禁用 1-启用", required = true) @RequestParam("status") Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.ok();
    }

    @ApiOperation("分页查询用户列表")
    @GetMapping("/list")
    @PreAuthorize("hasPermission(null, 'superadmin:role:query') || " +
                  "hasPermission(null, 'superadmin:dashboard:query') || " +
                  "hasPermission(null, 'superadmin:announcement:query')")
    public Result<IPage<UserDetailVO>> getUserList(
            @ApiParam(value = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword) {
        IPage<UserDetailVO> userList = userService.getUserList(pageNum, pageSize, keyword);
        return Result.ok(userList);
    }

    @ApiOperation("分配用户角色")
    @PostMapping("/assign-roles")
    @PreAuthorize("hasPermission(null, 'superadmin:role:update')")
    public Result<Void> assignUserRoles(@Validated @RequestBody AssignRoleDTO assignRoleDTO) {
        userService.assignUserRoles(assignRoleDTO);
        return Result.ok();
    }

    @ApiOperation("查询全部用户")
    @GetMapping("/all-user")
    @PreAuthorize("hasPermission(null, 'superadmin:role:query')")
    public Result<List<UserDetailVO>> allUsersDetail() {
        List<UserDetailVO> userDetailVOS = userService.getAllUserList();
        return Result.ok(userDetailVOS);
    }

    @ApiOperation("修改当前用户密码")
    @PutMapping("/password")
    public Result updatePassword(@Validated @RequestBody UpdatePasswordDTO passwordDTO) {
        // 获取当前用户
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();

        try {
            // 验证新密码与确认密码是否一致
            if (!RsaDecryptUtil.decryptString(passwordDTO.getNewPassword()).equals(RsaDecryptUtil.decryptString(passwordDTO.getConfirmPassword()))) {
                return Result.fail().message("新密码与确认密码不一致");
            }

            // 调用service层修改密码
            userService.updatePassword(
                    loginSysUser.getUserId(),
                    RsaDecryptUtil.decryptString(passwordDTO.getOldPassword()),
                    RsaDecryptUtil.decryptString(passwordDTO.getNewPassword())
            );
            return Result.ok();
        }catch (Exception e){
            throw new BusinessException("修改密码失败");
        }
    }

    @ApiOperation("更新当前用户信息")
    @PutMapping("/profile")
    public Result<Object> updateProfile(@Validated @RequestBody UpdateProfileDTO profileDTO) {
        // 获取当前用户
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();

        // 调用service层更新用户信息
        userService.updateProfile(loginSysUser.getUserId(), profileDTO);
        return Result.ok();
    }

    @ApiOperation("获取当前商户基本信息")
    @GetMapping("/merchant/profile")
    @PreAuthorize("hasPermission(null, 'merchant')")
    public Result<Object> merchantProfile() {
        // 获取当前用户
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();

        SysUser one = userService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, loginSysUser.getUserId()));
        if (one == null) {
            return Result.fail().message("异常状态");
        }

        // 调用service层更新用户信息
        return Result.ok(userService.merchantProfile(one));
    }

}
