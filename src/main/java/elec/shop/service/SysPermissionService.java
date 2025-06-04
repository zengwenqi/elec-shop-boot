package elec.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.dto.MenuVO;
import elec.shop.pojo.SysPermission;

import java.util.List;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【sys_permission(权限表)】的数据库操作Service
* @createDate 2025-06-04 14:16:09
*/
public interface SysPermissionService extends IService<SysPermission> {
    
    /**
     * 获取用户的菜单列表
     */
    List<MenuVO> getUserMenus(Long userId);
    
    /**
     * 获取用户的所有权限（包括按钮权限）
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户的菜单和权限信息
     */
    Map<String, Object> getUserMenusAndPermissions(Long userId);
    
    /**
     * 保存权限信息
     */
    void savePermission(SysPermission permission);
    
    /**
     * 更新权限信息
     */
    void updatePermission(SysPermission permission);
    
    /**
     * 删除权限
     */
    void deletePermission(Long permissionId);
}
