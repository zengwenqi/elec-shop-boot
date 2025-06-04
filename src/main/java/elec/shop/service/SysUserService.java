package elec.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.dto.RegisterRequest;
import elec.shop.dto.UserDetailVO;
import elec.shop.pojo.SysUser;

/**
* @author Lenovo
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2025-06-04 14:16:09
*/
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名获取用户信息
     */
    SysUser getUserByUsername(String username);

    /**
     * 获取用户的权限列表
     */
    String[] getUserPermissions(Long userId);

    /**
     * 注册新用户
     */
    Boolean registerUser(RegisterRequest request);

    /**
     * 获取用户详情
     */
    UserDetailVO getUserDetail(String username);

    /**
     * 根据用户ID获取用户详情
     */
    UserDetailVO getUserDetailById(Long userId);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 更新用户登录信息
     * @param userId 用户ID
     * @param ipAddress 登录IP地址
     */
    void updateLoginInfo(Long userId, String ipAddress);
}
