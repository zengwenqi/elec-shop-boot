package elec.shop.service.balance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.balance.GlobalCommissionConfig;
import elec.shop.pojo.balance.dto.GlobalCommissionConfigDTO;
import elec.shop.pojo.balance.vo.GlobalCommissionConfigVO;

import java.util.List;

/**
 * @author System
 * @description 针对表【global_commission_config(全局佣金配置表)】的数据库操作Service
 * @createDate 2025-01-16 00:00:00
 */
public interface GlobalCommissionConfigService extends IService<GlobalCommissionConfig> {
    
    /**
     * 分页查询全局佣金配置列表
     */
    IPage<GlobalCommissionConfigVO> getGlobalConfigPage(Integer pageNum, Integer pageSize,
                                                       Integer userType, Integer status, String keyword);
    
    /**
     * 查询全局佣金配置列表
     */
    List<GlobalCommissionConfigVO> getGlobalConfigList(Integer userType, Integer status, String keyword);
    
    /**
     * 根据ID查询全局佣金配置详情
     */
    GlobalCommissionConfigVO getGlobalConfigById(Long configId);
    
    /**
     * 创建全局佣金配置
     */
    Boolean createGlobalConfig(GlobalCommissionConfigDTO dto);
    
    /**
     * 更新全局佣金配置
     */
    Boolean updateGlobalConfig(Long configId, GlobalCommissionConfigDTO dto);
    
    /**
     * 删除全局佣金配置
     */
    Boolean deleteGlobalConfig(Long configId);
    
    /**
     * 批量删除全局佣金配置
     */
    Boolean batchDeleteGlobalConfig(List<Long> configIds);
    
    /**
     * 启用/禁用全局佣金配置
     */
    Boolean updateGlobalConfigStatus(Long configId, Integer status);
    
    /**
     * 根据用户类型查询有效的全局佣金配置
     */
    GlobalCommissionConfig getEffectiveConfigByUserType(Integer userType);
    
    /**
     * 校验全局佣金配置数据
     */
    void validateGlobalConfig(GlobalCommissionConfigDTO dto);
}