package elec.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.pojo.sys.dto.*;
import elec.shop.pojo.sys.SysUser;
import elec.shop.security.TokenManager;
import elec.shop.security.CustomUserDetails;
import elec.shop.security.AuthResponse;
import elec.shop.security.UserDetailsServiceImpl;
import elec.shop.service.sys.JwtConfigService;
import elec.shop.service.sys.SysPermissionService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.*;
import elec.shop.annotation.OperationLog;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;
    private final SysUserService userService;
    private final SysPermissionService permissionService;
    private final EmailUtil emailUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MinioUtil minioUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfigService jwtConfigService;

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
    public Result login(@RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        try {
            // 检查是否被锁定
            if (tokenManager.isLoginLocked(request.getUsername())) {
                return Result.fail("账户已被锁定，请"+jwtConfigService.getConfigValue("jwt.login-lock-duration")+"再试");
            }

            // 获取用户信息
            SysUser user = userService.getUserByUsername(request.getUsername());
            if (user == null) {
                tokenManager.recordLoginAttempt(request.getUsername());
                return Result.fail("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                tokenManager.recordLoginAttempt(request.getUsername());
                return Result.fail("用户名或密码错误");
            }

            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 更新登录信息
            String ipAddress = IpUtils.getClientIp(servletRequest);
            userService.updateLoginInfo(user.getUserId(), ipAddress);

            // 生成tokens
            String accessToken = tokenManager.generateAccessToken(userDetails);
            String refreshToken = tokenManager.generateRefreshToken(userDetails);

            // 存储到Redis
            tokenManager.storeRefreshToken(request.getUsername(), refreshToken);

            // 存储到Cookie
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 重置登录尝试次数
            tokenManager.resetLoginAttempts(request.getUsername());

            // 返回认证响应
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer ")
                    .expiresIn(900L) // 15分钟
                    .userId(userDetails.getUserId())
                    .username(userDetails.getUsername())
                    .avatar(minioUtil.getPreviewUrl(user.getAvatar()))
                    .message("登录成功")
                    .build();

            return Result.ok(authResponse);
        } catch (Exception e) {
            log.error("登录异常：{} - {}", request.getUsername(), e.getMessage());
            // 记录登录失败
            tokenManager.recordLoginAttempt(request.getUsername());
            return Result.fail("用户名或密码错误");
        }
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public Result refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 从cookie中获取refresh token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    try {
                        String username = tokenManager.extractClaim(refreshToken, Claims::getSubject);

                        // 验证Redis中存储的refreshToken是否匹配
                        if (!tokenManager.validateStoredRefreshToken(username, refreshToken)) {
                            return Result.fail("Refresh token已失效，请重新登录");
                        }

                        CustomUserDetails userDetails = (CustomUserDetails) userDetailsServiceImpl.loadUserByUsername(username);
                        SysUser user = userService.getUserByUsername(username);

                        if (tokenManager.validateRefreshToken(refreshToken, userDetails)) {
                            String newAccessToken = tokenManager.generateAccessToken(userDetails);

                            // 生成新的refresh token
                            String newRefreshToken = tokenManager.generateRefreshToken(userDetails);

                            // 更新Redis中的refresh token
                            tokenManager.storeRefreshToken(username, newRefreshToken);

                            // 更新cookie中的refresh token
                            ResponseCookie newCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                                    .httpOnly(true)
                                    .secure(true)
                                    .path("/")
                                    .maxAge(7 * 24 * 60 * 60)
                                    .sameSite("Strict")
                                    .build();
                            response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

                            AuthResponse authResponse = AuthResponse.builder()
                                    .accessToken(newAccessToken)
                                    .tokenType("Bearer ")
                                    .expiresIn(900L)
                                    .userId(userDetails.getUserId())
                                    .username(userDetails.getUsername())
                                    .avatar(minioUtil.getPreviewUrl(user.getAvatar()))
                                    .message("Token刷新成功")
                                    .build();

                            return Result.ok(authResponse);
                        }
                    } catch (Exception e) {
                        log.error("刷新token失败: {}", e.getMessage());
                        return Result.fail("刷新token失败");
                    }
                }
            }
        }
        return Result.fail("无效的refresh token");
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    @OperationLog(module = "认证管理", operationType = "退出", description = "退出登录", saveRequestData = false)
    public Result<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取当前用户名
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();
            String username = loginSysUser.getUsername();

            // 获取当前的access token
            String authHeader = request.getHeader("elec-shop-token");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                // 将token加入黑名单
                tokenManager.blacklistToken(accessToken);
            }

            // 如果能获取到用户名，清除Redis中的refresh token
            if (username != null) {
                tokenManager.invalidateRefreshToken(username);
            }

            // 清除refresh token cookie
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 清除安全上下文
            SecurityContextHolder.clearContext();

            return Result.ok("注销成功");
        } catch (Exception e) {
            log.error("退出登录失败: {}", e.getMessage());
            return Result.fail("退出登录失败");
        }
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
        emailUtil.sendEmail("注册账户",email);
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
