package elec.shop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenManager tokenManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && !tokenManager.isTokenBlacklisted(token)) {
                try {
                    // 验证 access token
                    String username = tokenManager.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (tokenManager.validateAccessToken(token, userDetails)) {
                        setAuthentication(userDetails,request);
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (ExpiredJwtException e) {
                    // access token 过期，检查是否可以刷新
                    try {
                        String username = e.getClaims().getSubject();
                        // 检查用户的refresh token是否有效
                        if (tokenManager.canRefreshToken(username)) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            // 生成新的 access token
                            String newAccessToken = tokenManager.generateAccessToken(userDetails);

                            // 在响应头中设置新的 token
                            response.setHeader("New-Access-Token", newAccessToken);
                            response.setHeader("Access-Control-Expose-Headers", "New-Access-Token");

                            setAuthentication(userDetails,request);
                            chain.doFilter(request, response);
                            return;
                        } else {
                            // refresh token 过期或无效
                            sendErrorResponse(response, 401003, "登录已过期，请重新登录");
                            return;
                        }
                    } catch (Exception refreshError) {
                        sendErrorResponse(response, 401003, "登录已过期，请重新登录");
                        return;
                    }
                }
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Token validation error", e);
            sendErrorResponse(response, 401002, "Token验证失败");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("elec-shop-token");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void setAuthentication(UserDetails userDetails,HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 检查请求是否在白名单中
     * @param requestPath 请求路径
     * @return 如果在白名单中返回true，否则返回false
     */
    private boolean isPermitAllRequest(String requestPath) {
        // 白名单路径列表
        final List<String> permitAllPaths = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/email/code",
            "/auth/email/verify",
            "/auth/reset-password",
            "/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/announcement",
            "/ws",
            "/ws/notice"
        );

        // 检查完整路径匹配
        if (permitAllPaths.contains(requestPath)) {
            return true;
        }

        // 检查路径前缀匹配
        return permitAllPaths.stream().anyMatch(path ->
            !path.equals("/") && requestPath.startsWith(path));
    }
}
