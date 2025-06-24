package elec.shop.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.sys.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysPermissionService permissionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 获取用户权限列表
        List<String> permissions = permissionService.getUserPermissions(user.getUserId());
        
        // 将权限列表存入Redis
        String permissionKey = "user:permissions:" + user.getUserId();
        redisTemplate.opsForValue().set(permissionKey, permissions, 24, TimeUnit.HOURS);

        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 返回自定义的UserDetails实现
        return new CustomUserDetails(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }

    public void clearUserPermissionsCache(Long userId) {
        String permissionKey = "user:permissions:" + userId;
        redisTemplate.delete(permissionKey);
    }
}
