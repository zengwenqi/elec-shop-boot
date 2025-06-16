package elec.shop.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.purchase.PurchaserInfoMapper;
import elec.shop.pojo.purchase.PurchaserInfo;
import elec.shop.pojo.sys.dto.RegisterRequest;
import elec.shop.pojo.sys.dto.UserDetailVO;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.sys.SysRoleMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.mapper.sys.SysUserRoleMapper;
import elec.shop.pojo.sys.SysRole;
import elec.shop.pojo.sys.SysUser;
import elec.shop.pojo.sys.SysUserRole;
import elec.shop.pojo.sys.enums.UserType;
import elec.shop.service.sys.SysPermissionService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.utils.AllContextUtils;
import elec.shop.pojo.sys.dto.AssignRoleDTO;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2025-06-04 14:16:09
*/
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final PurchaserInfoMapper purchaserInfoMapper;

    @Override
    public SysUser getUserByUsername(String username) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0)
        );
    }

    @Override
    public String[] getUserPermissions(Long userId) {
        List<String> permissions = permissionService.getUserPermissions(userId);
        return permissions.toArray(new String[0]);
    }

    @Override
    @Transactional
    public Boolean registerUser(RegisterRequest request) {
        // 检查用户名是否存在
        if (getUserByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名存在");
        }

        try {
            // 创建用户
            SysUser user = new SysUser();
            BeanUtils.copyProperties(request, user);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setStatus(1); // 默认启用
            user.setUserType(4); // 默认普通用户
            user.setCreatedAt(new Date());
            user.setIsDeleted(0);

            userMapper.insert(user);

            // 分配默认角色
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(4L);
            userRole.setCreatedAt(new Date());
            userRole.setIsDeleted(0);

            userRoleMapper.insert(userRole);

            return true;
        } catch (Exception e) {
            log.error("注册用户失败：", e);
            throw new BusinessException("注册用户失败");
        }
    }

    @Override
    public UserDetailVO getUserDetail(String username) {
        return getUserDetailById(getUserByUsername(username).getUserId());
    }

    @Override
    public UserDetailVO getUserDetailById(Long userId) {
        // 获取用户基本信息
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ResultCodeEnum.NONE_USER_ERROR);
        }

        // 获取用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getIsDeleted, 0)
        );

        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRole> roles = roleMapper.selectList(
            new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleId, roleIds)
                .eq(SysRole::getIsDeleted, 0)
        );

        // 构建返回对象
        UserDetailVO userDetail = new UserDetailVO();
        BeanUtils.copyProperties(user, userDetail);

        // 设置角色名称
        List<String> roleNames = roles.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toList());
        userDetail.setRoleNames(roleNames);

        // 设置权限列表
        List<String> permissions = permissionService.getUserPermissions(userId);
        userDetail.setPermissions(permissions);

        return userDetail;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ResultCodeEnum.NONE_USER_ERROR);
        }

        if (user.getUserType() == 1) {
            throw new BusinessException(ResultCodeEnum.PERMISSION);
        }

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getUserId, userId)
                .set(SysUser::getStatus, status)
                .set(SysUser::getUpdatedAt, new Date());

        userMapper.update(null, updateWrapper);
    }

    @Override
    public void updateLoginInfo(Long userId, String ipAddress) {
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getUserId, userId)
                .set(SysUser::getLastLoginTime, new Date())
                .set(SysUser::getLastLoginIp, ipAddress)
                .set(SysUser::getUpdatedAt, new Date());

        userMapper.update(null, updateWrapper);
    }

    @Override
    public IPage<UserDetailVO> getUserList(Integer pageNum, Integer pageSize, String keyword) {
        // 构建查询条件
        // 默认不返回最高管理员数据
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getIsDeleted, 0)
                .ne(SysUser::getUserType, 1)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getRealName, keyword)
                        .or()
                        .like(SysUser::getMobile, keyword)
                )
                .orderByDesc(SysUser::getCreatedAt);

        // 执行分页查询
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = userMapper.selectPage(page, queryWrapper);

        // 转换为UserDetailVO
        return userPage.convert(user -> {
            // 默认不返回最高管理员数据
            UserDetailVO userDetail = new UserDetailVO();
            BeanUtils.copyProperties(user, userDetail);
//            userDetail.setUserTypeName(UserType.getById(user.getUserType()).getName());
            // 获取用户角色
            List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, user.getUserId())
                    .eq(SysUserRole::getIsDeleted, 0)
            );

            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());

            if (!roleIds.isEmpty()) {
                List<SysRole> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleId, roleIds)
                        .eq(SysRole::getIsDeleted, 0)
                );

                List<String> roleNames = roles.stream()
                        .map(SysRole::getRoleName)
                        .collect(Collectors.toList());
                userDetail.setRoleNames(roleNames);
            }

            // 获取用户权限
            List<String> permissions = permissionService.getUserPermissions(user.getUserId());
            userDetail.setPermissions(permissions);

            return userDetail;
        });
    }

    @Override
    @Transactional
    public void assignUserRoles(AssignRoleDTO assignRoleDTO) {
        Long userId = assignRoleDTO.getUserId();
        Long roleId = assignRoleDTO.getRoleId();

        // 检查用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ResultCodeEnum.NONE_USER_ERROR);
        }

        // 如果是超级管理员，不允许修改角色
        if (user.getUserType() == 1) {
            throw new BusinessException(ResultCodeEnum.PERMISSION);
        }

        // 判断用户之前是否是采购员
        if (user.getUserType()==2){
            purchaserInfoMapper.delete(new LambdaQueryWrapper<PurchaserInfo>()
                    .eq(PurchaserInfo::getUserId,userId));
        }

        // 检查角色是否存在且有效
        if (roleId!=null) {
            Long l = roleMapper.selectCount(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleId, roleId)
                            .eq(SysRole::getStatus, 1)
            );

            if (l==0L) {
                throw new BusinessException("存在无效的角色ID");
            }
        }

        // 分配新角色
        if (roleId!=null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);

            userRoleMapper.update(userRole,new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            SysUser sysUser = new SysUser();
            sysUser.setUserType(roleId.intValue());
            userMapper.update(sysUser,new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getUserId, userId));
        }

        // 如果分配采购员，则要去添加数据
        if (roleId==3L){
            PurchaserInfo purchaserInfo = new PurchaserInfo();
            purchaserInfo.setUserId(userId);
            purchaserInfo.setPurchaserCode(AllContextUtils.generatePurchaserCode(userId));
            purchaserInfoMapper.insert(purchaserInfo);
        }
    }
}




