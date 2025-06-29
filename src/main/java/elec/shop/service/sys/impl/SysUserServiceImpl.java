package elec.shop.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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
import elec.shop.service.purchase.ShopInfoService;
import elec.shop.service.sys.SysPermissionService;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.MinioUtil;
import elec.shop.utils.ResultCodeEnum;
import elec.shop.utils.RsaDecryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.utils.AllContextUtils;
import elec.shop.pojo.sys.dto.AssignRoleDTO;
import elec.shop.pojo.sys.dto.UpdateProfileDTO;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private final ShopInfoService shopInfoService;
    private final MinioUtil minioUtil;

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
            request.setPassword(RsaDecryptUtil.decryptString(request.getPassword()));
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

            shopInfoService.initUserInfoData(user.getUserId());
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
        userDetail.setAvatar(minioUtil.getPreviewUrl(user.getAvatar()));

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

    @Override
    public List<UserDetailVO> getAllUserList() {
        // 构建查询条件，默认不返回最高管理员数据
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getIsDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt);

        // 执行查询
        List<SysUser> userList = userMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }

        // 批量获取所有用户的角色
        Map<Long, List<SysRole>> userRoleMap = getUserRoleMap(userList);

        // 转换为UserDetailVO
        return userList.stream()
                .map(user -> {
                    UserDetailVO userDetail = new UserDetailVO();
                    BeanUtils.copyProperties(user, userDetail);

                    // 设置角色名称
                    List<SysRole> roles = userRoleMap.getOrDefault(user.getUserId(), Collections.emptyList());
                    List<String> roleNames = roles.stream()
                            .map(SysRole::getRoleName)
                            .collect(Collectors.toList());
                    userDetail.setRoleNames(roleNames);

                    // 获取用户权限
                    List<String> permissions = permissionService.getUserPermissions(user.getUserId());
                    userDetail.setPermissions(permissions);

                    return userDetail;
                })
                .collect(Collectors.toList());
    }

    // 批量获取用户角色的辅助方法
    private Map<Long, List<SysRole>> getUserRoleMap(List<SysUser> userList) {
        // 提取所有用户ID
        List<Long> userIds = userList.stream()
                .map(SysUser::getUserId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量查询所有用户的角色关联
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getUserId, userIds)
                        .eq(SysUserRole::getIsDeleted, 0)
        );

        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyMap();
        }

        // 提取所有角色ID
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 批量查询所有角色信息
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleId, roleIds)
                        .eq(SysRole::getIsDeleted, 0)
        );

        Map<Long, SysRole> roleMap = roles.stream()
                .collect(Collectors.toMap(SysRole::getRoleId, role -> role));

        // 构建用户ID到角色列表的映射
        return userRoles.stream()
                .filter(ur -> roleMap.containsKey(ur.getRoleId()))
                .collect(Collectors.groupingBy(
                        SysUserRole::getUserId,
                        Collectors.mapping(ur -> roleMap.get(ur.getRoleId()), Collectors.toList())
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户信息
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getUserId, userId)
                .one();

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证原密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 更新密码
        user.setPassword(encodedPassword);
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileDTO profileDTO) {
        // 查询用户信息
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        user.setRealName(profileDTO.getRealName());
        user.setMobile(profileDTO.getMobile());
        user.setEmail(profileDTO.getEmail());
        user.setAvatar(profileDTO.getAvatar());
        user.setUpdatedAt(new Date());

        // 保存更新
        this.updateById(user);
    }

    @Override
    public Boolean resetPassword(String email, String newPassword) {
        // 根据邮箱查找用户
        SysUser user = baseMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email)
        );

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        try {
            // 更新密码
            user.setPassword(passwordEncoder.encode(RsaDecryptUtil.decryptString(newPassword)));
            return baseMapper.updateById(user) > 0;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public SysUser getUserByEmail(String email) {
        return baseMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email)
                .eq(SysUser::getIsDeleted, 0)
        );
    }

    @Override
    public boolean checkPaymentPasswordExists() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId, loginSysUser.getUserId())
                .select(SysUser::getPaymentPassword));
        return user != null && org.springframework.util.StringUtils.hasText(user.getPaymentPassword());
    }

    @Override
    public boolean verifyPaymentPassword(String paymentPassword) {
        if (!org.springframework.util.StringUtils.hasText(paymentPassword)) {
            return false;
        }
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId, loginSysUser.getUserId())
                .select(SysUser::getPaymentPassword));

        if (user == null || !org.springframework.util.StringUtils.hasText(user.getPaymentPassword())) {
            return false;
        }

        return passwordEncoder.matches(paymentPassword, user.getPaymentPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPaymentPassword(String paymentPassword) {
        if (!org.springframework.util.StringUtils.hasText(paymentPassword)) {
            throw new IllegalArgumentException("支付密码不能为空");
        }

        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        // 获取用户信息
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId, loginSysUser.getUserId()));

        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 加密支付密码
        String encodedPassword = passwordEncoder.encode(paymentPassword);

        // 更新支付密码
        user.setPaymentPassword(encodedPassword);
        updateById(user);
    }
}




