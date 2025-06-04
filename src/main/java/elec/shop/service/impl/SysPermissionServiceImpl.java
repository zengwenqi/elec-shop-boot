package elec.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.dto.MenuVO;
import elec.shop.mapper.SysPermissionMapper;
import elec.shop.mapper.SysRolePermissionMapper;
import elec.shop.mapper.SysUserRoleMapper;
import elec.shop.pojo.SysPermission;
import elec.shop.pojo.SysRolePermission;
import elec.shop.pojo.SysUserRole;
import elec.shop.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【sys_permission(权限表)】的数据库操作Service实现
* @createDate 2025-06-04 14:16:09
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
    implements SysPermissionService{

    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public List<MenuVO> getUserMenus(Long userId) {
        // 1. 获取用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getIsDeleted, 0)
        );

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取角色权限
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds)
                .eq(SysRolePermission::getIsDeleted, 0)
        );

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 获取菜单权限
        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<SysPermission> permissions = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getPermissionId, permissionIds)
                .eq(SysPermission::getPermissionType, 1) // 菜单类型
                .eq(SysPermission::getStatus, 1) // 启用状态
                .eq(SysPermission::getIsDeleted, 0)
                .orderByAsc(SysPermission::getSortOrder)
        );

        // 4. 构建菜单树
        return buildMenuTree(permissions, 0L);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 1. 获取用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getIsDeleted, 0)
        );

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取角色权限
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds)
                .eq(SysRolePermission::getIsDeleted, 0)
        );

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 获取所有权限
        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<SysPermission> permissions = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getPermissionId, permissionIds)
                .eq(SysPermission::getStatus, 1)
                .eq(SysPermission::getIsDeleted, 0)
        );

        return permissions.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void savePermission(SysPermission permission) {
        permission.setCreatedAt(new Date());
        permission.setIsDeleted(0);
        permissionMapper.insert(permission);
    }

    @Override
    @Transactional
    public void updatePermission(SysPermission permission) {
        permission.setUpdatedAt(new Date());
        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        // 逻辑删除权限
        LambdaUpdateWrapper<SysPermission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysPermission::getPermissionId, permissionId)
                .set(SysPermission::getIsDeleted, 1)
                .set(SysPermission::getDeletedAt, new Date());
        permissionMapper.update(null, updateWrapper);

        // 删除角色-权限关联
        LambdaUpdateWrapper<SysRolePermission> rolePermissionUpdateWrapper = new LambdaUpdateWrapper<>();
        rolePermissionUpdateWrapper.eq(SysRolePermission::getPermissionId, permissionId)
                .set(SysRolePermission::getIsDeleted, 1);
        rolePermissionMapper.update(null, rolePermissionUpdateWrapper);
    }

    private List<MenuVO> buildMenuTree(List<SysPermission> permissions, Long parentId) {
        List<MenuVO> menus = new ArrayList<>();

        for (SysPermission permission : permissions) {
            if (permission.getParentId().equals(parentId)) {
                MenuVO menu = new MenuVO();
                BeanUtils.copyProperties(permission, menu);
                menu.setId(permission.getPermissionId());
                menu.setHidden(permission.getHidden() == 1);
                menu.setKeepAlive(permission.getKeepAlive() == 1);

                // 递归获取子菜单
                List<MenuVO> children = buildMenuTree(permissions, permission.getPermissionId());
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }

                menus.add(menu);
            }
        }

        return menus;
    }

    @Override
    public Map<String, Object> getUserMenusAndPermissions(Long userId) {
        // 1. 获取菜单列表
        List<MenuVO> allMenus = getUserMenus(userId);

        // 2. 构建树形菜单
        List<MenuVO> menuTree = buildMenuTree(allMenus);

        // 3. 获取权限列表
        List<String> permissions = getUserPermissions(userId);

        // 4. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("menus", menuTree);
        result.put("permissions", permissions);

        return result;
    }

    /**
     * 构建树形菜单
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 按父ID分组
        Map<Long, List<MenuVO>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(MenuVO::getParentId));

        // 2. 递归设置子菜单
        return menus.stream()
                .filter(menu -> menu.getParentId() == 0) // 获取顶级菜单
                .peek(menu -> setChildren(menu, menuMap))
                .sorted(Comparator.comparing(MenuVO::getSortOrder)) // 按排序号排序
                .collect(Collectors.toList());
    }

    /**
     * 递归设置子菜单
     */
    private void setChildren(MenuVO menu, Map<Long, List<MenuVO>> menuMap) {
        List<MenuVO> children = menuMap.get(menu.getId());
        if (children != null) {
            // 排序
            children.sort(Comparator.comparing(MenuVO::getSortOrder));
            // 设置子菜单
            menu.setChildren(children);
            // 递归处理每个子菜单
            children.forEach(child -> setChildren(child, menuMap));
        }
    }
}




