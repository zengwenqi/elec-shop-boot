package elec.shop.service.balance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.balance.UserCommissionConfig;
import elec.shop.pojo.balance.dto.UserCommissionConfigDTO;
import elec.shop.pojo.balance.vo.UserCommissionConfigVO;

import java.util.List;

/**
 * @author System
 * @description 针对表【user_commission_config(用户佣金配置表)】的数据库操作Service
 * @createDate 2025-01-16 00:00:00
 */
public interface UserCommissionConfigService extends IService<UserCommissionConfig> {
    
    /**
     * 分页查询用户佣金配置列表
     */
    IPage<UserCommissionConfigVO> getUserConfigPage(Integer pageNum, Integer pageSize,
                                                   Integer userType, Integer status, String keyword);
    
    /**
     * 查询用户佣金配置列表
     */
    List<UserCommissionConfigVO> getUserConfigList(Integer userType, Integer status, String keyword);
    
    /**
     * 根据ID查询用户佣金配置详情
     */
    UserCommissionConfigVO getUserConfigById(Long configId);
    
    /**
     * 根据用户ID查询佣金配置
     */
    UserCommissionConfigVO getUserConfigByUserId(Long userId);
    
    /**
     * 创建用户佣金配置
     */
    Boolean createUserConfig(UserCommissionConfigDTO dto);
    
    /**
     * 更新用户佣金配置
     */
    Boolean updateUserConfig(Long configId, UserCommissionConfigDTO dto);
    
    /**
     * 删除用户佣金配置
     */
    Boolean deleteUserConfig(Long configId);
    
    /**
     * 批量删除用户佣金配置
     */
    Boolean batchDeleteUserConfig(List<Long> configIds);
    
    /**
     * 启用/禁用用户佣金配置
     */
    Boolean updateUserConfigStatus(Long configId, Integer status);
    
    /**
     * 根据用户ID查询有效的佣金配置
     */
    UserCommissionConfig getEffectiveConfigByUserId(Long userId);
    
    /**
     * 批量查询用户信息用于搜索
     */
    List<UserCommissionConfigVO> searchUsers(String keyword, Integer userType);
    
    /**
     * 校验用户佣金配置数据
     */
    void validateUserConfig(UserCommissionConfigDTO dto);
}