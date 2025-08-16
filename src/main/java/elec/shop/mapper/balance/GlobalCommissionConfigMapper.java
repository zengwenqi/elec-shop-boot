package elec.shop.mapper.balance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.balance.GlobalCommissionConfig;
import elec.shop.pojo.balance.vo.GlobalCommissionConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author System
 * @description 针对表【global_commission_config(全局佣金配置表)】的数据库操作Mapper
 * @createDate 2025-01-16 00:00:00
 * @Entity elec.shop.pojo.balance.GlobalCommissionConfig
 */
@Mapper
public interface GlobalCommissionConfigMapper extends BaseMapper<GlobalCommissionConfig> {
    
    /**
     * 分页查询全局佣金配置列表
     */
    IPage<GlobalCommissionConfigVO> selectGlobalConfigPage(IPage<GlobalCommissionConfigVO> page,
                                                           @Param("userType") Integer userType,
                                                           @Param("status") Integer status,
                                                           @Param("keyword") String keyword);
    
    /**
     * 查询全局佣金配置列表
     */
    List<GlobalCommissionConfigVO> selectGlobalConfigList(@Param("userType") Integer userType,
                                                          @Param("status") Integer status,
                                                          @Param("keyword") String keyword);
    
    /**
     * 根据用户类型查询有效的全局佣金配置
     */
    GlobalCommissionConfig selectEffectiveConfigByUserType(@Param("userType") Integer userType);
    
    /**
     * 查询全局佣金配置详情
     */
    GlobalCommissionConfigVO selectGlobalConfigById(@Param("configId") Long configId);
}