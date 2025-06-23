package elec.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.pojo.sys.dto.*;
import elec.shop.pojo.sys.SysUser;
import elec.shop.security.JwtUtils;
import elec.shop.service.sys.SysPermissionService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.EmailUtil;
import elec.shop.utils.IpUtils;
import elec.shop.utils.Result;
import elec.shop.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final SysPermissionService permissionService;
    private final EmailUtil emailUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    @OperationLog(module = "认证管理", operationType = "注册", description = "用户注册")
    public Result register(@RequestBody RegisterRequest request) {
        if (!(request.getEmailCode().equals(redisTemplate.opsForValue().get(request.getEmail()))))
            return Result.fail().message("邮箱验证码错误");
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
    public Result<Map<String, Object>> getUserMenu() throws JsonProcessingException {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        String cacheKey = "user:menu:" + loginSysUser.getUserId();

        // 尝试从Redis获取缓存
        String cachedResult = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            // 反序列化为Map
            Map<String, Object> resultMap = objectMapper.readValue(cachedResult,
                    new TypeReference<Map<String, Object>>() {});
            return Result.ok(resultMap);
        }

        // 缓存未命中，查询数据库
        Map<String, Object> result = permissionService.getUserMenusAndPermissions(loginSysUser.getUserId());

        // 将结果存入Redis，设置1小时过期
        redisTemplate.opsForValue().set(
                cacheKey,
                objectMapper.writeValueAsString(result),
                1,
                TimeUnit.HOURS
        );
        return Result.ok(result);
    }


    @ApiOperation("获取邮箱验证码")
    @GetMapping("/email/code")
    public Result getEmailCode(@RequestParam("email") String email) {
        long count = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email));
        if (count==1){
            return Result.fail().message("该邮箱已绑定其他账户");
        }
        emailUtil.sendEmail(email);
        return Result.ok();
    }

    @ApiOperation("验证邮箱验证码")
    @PostMapping("/email/verify")
    public Result verifyEmailCode(@RequestBody EmailVerifyRequest request) {
        // 验证码校验
        String cachedCode = (String) redisTemplate.opsForValue().get(request.getEmail());
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            return Result.fail().message("验证码错误或已过期");
        }

        // 获取邮箱绑定的用户信息
        SysUser user = userService.getUserByEmail(request.getEmail());
        if (user == null) {
            return Result.fail().message("该邮箱未绑定任何账户");
        }

        EmailVerifyResponse response = EmailVerifyResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return Result.ok(response);
    }

    @ApiOperation("重置密码")
    @PostMapping("/reset-password")
    @OperationLog(module = "认证管理", operationType = "重置密码", description = "重置密码")
    public Result resetPassword(@RequestBody ResetPasswordRequest request) {
        // 验证邮箱验证码
        String cachedCode = (String) redisTemplate.opsForValue().get(request.getEmail());
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            return Result.fail().message("验证码错误或已过期");
        }

        // 重置密码
        Boolean result = userService.resetPassword(request.getEmail(), request.getNewPassword());
        if (result) {
            // 删除验证码缓存
            redisTemplate.delete(request.getEmail());
            return Result.ok();
        }
        return Result.fail().message("重置密码失败");
    }
}
