package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.balance.UserCommissionConfigMapper;
import elec.shop.pojo.balance.UserCommissionConfig;
import elec.shop.pojo.balance.dto.UserCommissionConfigDTO;
import elec.shop.pojo.balance.enums.CommissionTypeEnum;
import elec.shop.pojo.balance.enums.UserTypeEnum;
import elec.shop.pojo.balance.vo.UserCommissionConfigVO;
import elec.shop.service.balance.UserCommissionConfigService;
import elec.shop.utils.AllContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author System
 * @description 针对表【user_commission_config(用户佣金配置表)】的数据库操作Service实现
 * @createDate 2025-01-16 00:00:00
 */
@Slf4j
@Service
public class UserCommissionConfigServiceImpl extends ServiceImpl<UserCommissionConfigMapper, UserCommissionConfig>
        implements UserCommissionConfigService {

    @Override
    public IPage<UserCommissionConfigVO> getUserConfigPage(Integer pageNum, Integer pageSize,
                                                          Integer userType, Integer status, String keyword) {
        Page<UserCommissionConfigVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectUserConfigPage(page, userType, status, keyword);
    }

    @Override
    public List<UserCommissionConfigVO> getUserConfigList(Integer userType, Integer status, String keyword) {
        return baseMapper.selectUserConfigList(userType, status, keyword);
    }

    @Override
    public UserCommissionConfigVO getUserConfigById(Long configId) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }
        return baseMapper.selectUserConfigById(configId);
    }

    @Override
    public UserCommissionConfigVO getUserConfigByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return baseMapper.selectUserConfigByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createUserConfig(UserCommissionConfigDTO dto) {
        // 校验数据
        validateUserConfig(dto);

        // 检查用户是否已存在配置
        LambdaQueryWrapper<UserCommissionConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCommissionConfig::getUserId, dto.getUserId())
               .eq(UserCommissionConfig::getStatus, 1)
               .eq(UserCommissionConfig::getIsDeleted, 0);

        UserCommissionConfig existConfig = this.getOne(wrapper);
        if (existConfig != null) {
            throw new BusinessException("该用户已存在启用状态的佣金配置");
        }

        // 创建配置
        UserCommissionConfig config = new UserCommissionConfig();
        BeanUtils.copyProperties(dto, config);
        config.setCreatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setCreatedAt(new Date());

        return this.save(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserConfig(Long configId, UserCommissionConfigDTO dto) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }

        // 校验数据
        validateUserConfig(dto);

        // 检查配置是否存在
        UserCommissionConfig existConfig = this.getById(configId);
        if (existConfig == null) {
            throw new BusinessException("用户佣金配置不存在");
        }

        // 检查用户是否已存在其他启用配置（排除当前配置）
        LambdaQueryWrapper<UserCommissionConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCommissionConfig::getUserId, dto.getUserId())
               .eq(UserCommissionConfig::getStatus, 1)
               .eq(UserCommissionConfig::getIsDeleted, 0)
               .ne(UserCommissionConfig::getConfigId, configId);

        UserCommissionConfig duplicateConfig = this.getOne(wrapper);
        if (duplicateConfig != null && dto.getStatus() == 1) {
            throw new BusinessException("该用户已存在启用状态的佣金配置");
        }

        // 更新配置
        UserCommissionConfig config = new UserCommissionConfig();
        BeanUtils.copyProperties(dto, config);
        config.setConfigId(configId);
        config.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setUpdatedAt(new Date());

        return this.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUserConfig(Long configId) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }

        UserCommissionConfig config = this.getById(configId);
        if (config == null) {
            throw new BusinessException("用户佣金配置不存在");
        }

        // 逻辑删除
        config.setIsDeleted(1);
        config.setDeletedAt(new Date());
        config.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setUpdatedAt(new Date());

        return this.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteUserConfig(List<Long> configIds) {
        if (CollectionUtils.isEmpty(configIds)) {
            throw new BusinessException("配置ID列表不能为空");
        }

        for (Long configId : configIds) {
            deleteUserConfig(configId);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserConfigStatus(Long configId, Integer status) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效");
        }

        UserCommissionConfig config = this.getById(configId);
        if (config == null) {
            throw new BusinessException("用户佣金配置不存在");
        }

        // 如果要启用，检查该用户是否已有启用的配置
        if (status == 1) {
            LambdaQueryWrapper<UserCommissionConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCommissionConfig::getUserId, config.getUserId())
                   .eq(UserCommissionConfig::getStatus, 1)
                   .eq(UserCommissionConfig::getIsDeleted, 0)
                   .ne(UserCommissionConfig::getConfigId, configId);

            UserCommissionConfig existConfig = this.getOne(wrapper);
            if (existConfig != null) {
                throw new BusinessException("该用户已存在启用状态的佣金配置");
            }
        }

        config.setStatus(status);
        config.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setUpdatedAt(new Date());

        return this.updateById(config);
    }

    @Override
    public UserCommissionConfig getEffectiveConfigByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return baseMapper.selectEffectiveConfigByUserId(userId);
    }

    @Override
    public List<UserCommissionConfigVO> searchUsers(String keyword, Integer userType) {
        return baseMapper.searchUsers(keyword, userType);
    }

    @Override
    public void validateUserConfig(UserCommissionConfigDTO dto) {
        if (dto == null) {
            throw new BusinessException("配置数据不能为空");
        }

        if (dto.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }

        if (dto.getUserType() == null) {
            throw new BusinessException("用户类型不能为空");
        }

        if (UserTypeEnum.getByCode(dto.getUserType()) == null) {
            throw new BusinessException("用户类型无效");
        }

        if (dto.getCommissionType() == null) {
            throw new BusinessException("佣金类型不能为空");
        }

        if (CommissionTypeEnum.getByCode(dto.getCommissionType()) == null) {
            throw new BusinessException("佣金类型无效");
        }

        if (dto.getCommissionValue() == null || dto.getCommissionValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("佣金值必须大于0");
        }

        // 百分比类型的佣金值不能超过100%
        if (dto.getCommissionType() == 2 && dto.getCommissionValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("百分比佣金值不能超过100%");
        }

        if (dto.getMinCommission() != null && dto.getMinCommission().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("最小佣金金额不能小于0");
        }

        if (dto.getMaxCommission() != null && dto.getMaxCommission().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("最大佣金金额不能小于0");
        }

        if (dto.getMinCommission() != null && dto.getMaxCommission() != null
            && dto.getMinCommission().compareTo(dto.getMaxCommission()) > 0) {
            throw new BusinessException("最小佣金金额不能大于最大佣金金额");
        }

        if (dto.getEffectiveTime() != null && dto.getExpiryTime() != null
            && dto.getEffectiveTime().after(dto.getExpiryTime())) {
            throw new BusinessException("生效时间不能晚于失效时间");
        }

        if (dto.getStatus() == null || (dto.getStatus() != 0 && dto.getStatus() != 1)) {
            throw new BusinessException("状态值无效");
        }
    }
}
