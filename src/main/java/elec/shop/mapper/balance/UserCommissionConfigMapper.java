package elec.shop.mapper.balance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.balance.UserCommissionConfig;
import elec.shop.pojo.balance.vo.UserCommissionConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author System
 * @description 针对表【user_commission_config(用户佣金配置表)】的数据库操作Mapper
 * @createDate 2025-01-16 00:00:00
 * @Entity elec.shop.pojo.balance.UserCommissionConfig
 */
@Mapper
public interface UserCommissionConfigMapper extends BaseMapper<UserCommissionConfig> {
    
    /**
     * 分页查询用户佣金配置列表
     */
    IPage<UserCommissionConfigVO> selectUserConfigPage(IPage<UserCommissionConfigVO> page,
                                                       @Param("userType") Integer userType,
                                                       @Param("status") Integer status,
                                                       @Param("keyword") String keyword);
    
    /**
     * 查询用户佣金配置列表
     */
    List<UserCommissionConfigVO> selectUserConfigList(@Param("userType") Integer userType,
                                                      @Param("status") Integer status,
                                                      @Param("keyword") String keyword);
    
    /**
     * 根据用户ID查询有效的佣金配置
     */
    UserCommissionConfig selectEffectiveConfigByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户佣金配置详情
     */
    UserCommissionConfigVO selectUserConfigById(@Param("configId") Long configId);
    
    /**
     * 根据用户ID查询佣金配置
     */
    UserCommissionConfigVO selectUserConfigByUserId(@Param("userId") Long userId);
    
    /**
     * 批量查询用户信息用于搜索
     */
    List<UserCommissionConfigVO> searchUsers(@Param("keyword") String keyword,
                                             @Param("userType") Integer userType);
}