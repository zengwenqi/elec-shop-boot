package elec.shop.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.sys.dto.RegisterRequest;
import elec.shop.pojo.sys.dto.UserDetailVO;
import elec.shop.pojo.sys.SysUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.sys.dto.AssignRoleDTO;
import elec.shop.pojo.sys.dto.UpdateProfileDTO;

import java.util.List;

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

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词（用户名、真实姓名、手机号）
     * @return 分页用户列表
     */
    IPage<UserDetailVO> getUserList(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 分配用户角色
     * @param assignRoleDTO 分配角色请求参数
     */
    void assignUserRoles(AssignRoleDTO assignRoleDTO);

    /**
     * 获取全部用户列表
     * @return 全部用户列表
     */
    List<UserDetailVO> getAllUserList();

    /**
     * 修改用户密码
     *
     * @param userId    用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户个人信息
     *
     * @param userId 用户ID
     * @param profileDTO 用户信息
     */
    void updateProfile(Long userId, UpdateProfileDTO profileDTO);

    /**
     * 重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     * @return 是否成功
     */
    Boolean resetPassword(String email, String newPassword);

    /**
     * 根据邮箱获取用户信息
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser getUserByEmail(String email);

    /**
     * 检查用户是否设置了支付密码
     *
     * @return true-已设置 false-未设置
     */
    boolean checkPaymentPasswordExists();

    /**
     * 验证支付密码是否正确
     *
     * @param paymentPassword 支付密码
     * @return true-正确 false-错误
     */
    boolean verifyPaymentPassword(String paymentPassword);

    /**
     * 设置支付密码
     *
     * @param paymentPassword 支付密码
     */
    void setPaymentPassword(String paymentPassword);
}
