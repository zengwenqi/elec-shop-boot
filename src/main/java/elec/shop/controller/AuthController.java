package elec.shop.controller;

import elec.shop.pojo.sys.dto.LoginRequest;
import elec.shop.pojo.sys.dto.LoginResponse;
import elec.shop.pojo.sys.dto.RegisterRequest;
import elec.shop.pojo.sys.SysUser;
import elec.shop.security.JwtUtils;
import elec.shop.service.sys.SysPermissionService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.IpUtils;
import elec.shop.utils.Result;
import elec.shop.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final SysPermissionService permissionService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    @OperationLog(module = "认证管理", operationType = "注册", description = "用户注册")
    public Result register(@RequestBody RegisterRequest request) {
        Boolean result = userService.registerUser(request);
        if (result){
            return Result.ok();
        }
        return Result.fail().message("注册失败");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    @OperationLog(module = "认证管理", operationType = "登录", description = "用户登录", isLogin = true)
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtUtils.generateToken(userDetails);
        SysUser user = userService.getUserByUsername(request.getUsername());

        String ipAddress = IpUtils.getClientIp(servletRequest);
        userService.updateLoginInfo(user.getUserId(), ipAddress);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getUserId())
                .build();

        return Result.ok(response);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    @OperationLog(module = "认证管理", operationType = "退出", description = "退出登录")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.ok();
    }

    @ApiOperation("获取用户菜单和权限信息")
    @GetMapping("/menu")
    @OperationLog(module = "认证管理", operationType = "动态菜单", description = "获取动态菜单数据", saveResponseData = false)
    public Result<Map<String, Object>> getUserMenu() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        Map<String, Object> result = permissionService.getUserMenusAndPermissions(loginSysUser.getUserId());
        return Result.ok(result);
    }
}
