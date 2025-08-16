package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.balance.GlobalCommissionConfigMapper;
import elec.shop.pojo.balance.GlobalCommissionConfig;
import elec.shop.pojo.balance.dto.GlobalCommissionConfigDTO;
import elec.shop.pojo.balance.enums.CommissionTypeEnum;
import elec.shop.pojo.balance.enums.UserTypeEnum;
import elec.shop.pojo.balance.vo.GlobalCommissionConfigVO;
import elec.shop.service.balance.GlobalCommissionConfigService;
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
 * @description 针对表【global_commission_config(全局佣金配置表)】的数据库操作Service实现
 * @createDate 2025-01-16 00:00:00
 */
@Slf4j
@Service
public class GlobalCommissionConfigServiceImpl extends ServiceImpl<GlobalCommissionConfigMapper, GlobalCommissionConfig>
        implements GlobalCommissionConfigService {

    @Override
    public IPage<GlobalCommissionConfigVO> getGlobalConfigPage(Integer pageNum, Integer pageSize,
                                                              Integer userType, Integer status, String keyword) {
        Page<GlobalCommissionConfigVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectGlobalConfigPage(page, userType, status, keyword);
    }

    @Override
    public List<GlobalCommissionConfigVO> getGlobalConfigList(Integer userType, Integer status, String keyword) {
        return baseMapper.selectGlobalConfigList(userType, status, keyword);
    }

    @Override
    public GlobalCommissionConfigVO getGlobalConfigById(Long configId) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }
        return baseMapper.selectGlobalConfigById(configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createGlobalConfig(GlobalCommissionConfigDTO dto) {
        // 校验数据
        validateGlobalConfig(dto);

        // 检查同类型配置是否已存在
        LambdaQueryWrapper<GlobalCommissionConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(GlobalCommissionConfig::getConfigName, dto.getConfigName())
               .eq(GlobalCommissionConfig::getStatus, 1)
               .eq(GlobalCommissionConfig::getIsDeleted, 0);

        GlobalCommissionConfig existConfig = this.getOne(wrapper);
        if (existConfig != null) {
            throw new BusinessException("该用户类型已存在启用状态的全局佣金配置");
        }

        // 创建配置
        GlobalCommissionConfig config = new GlobalCommissionConfig();
        BeanUtils.copyProperties(dto, config);
        config.setCreatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setCreatedAt(new Date());

        return this.save(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGlobalConfig(Long configId, GlobalCommissionConfigDTO dto) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }

        // 校验数据
        validateGlobalConfig(dto);

        // 检查配置是否存在
        GlobalCommissionConfig existConfig = this.getById(configId);
        if (existConfig == null) {
            throw new BusinessException("全局佣金配置不存在");
        }

        // 检查同类型配置是否已存在（排除当前配置）
        LambdaQueryWrapper<GlobalCommissionConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GlobalCommissionConfig::getConfigName, dto.getConfigName())
               .eq(GlobalCommissionConfig::getStatus, 1)
               .eq(GlobalCommissionConfig::getIsDeleted, 0)
               .ne(GlobalCommissionConfig::getConfigId, configId);

        GlobalCommissionConfig duplicateConfig = this.getOne(wrapper);
        if (duplicateConfig != null && dto.getStatus() == 1) {
            throw new BusinessException("该用户类型已存在启用状态的全局佣金配置");
        }

        // 更新配置
        GlobalCommissionConfig config = new GlobalCommissionConfig();
        BeanUtils.copyProperties(dto, config);
        config.setConfigId(configId);
        config.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setUpdatedAt(new Date());

        return this.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGlobalConfig(Long configId) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }

        GlobalCommissionConfig config = this.getById(configId);
        if (config == null) {
            throw new BusinessException("全局佣金配置不存在");
        }

        return this.removeById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteGlobalConfig(List<Long> configIds) {
        if (CollectionUtils.isEmpty(configIds)) {
            throw new BusinessException("配置ID列表不能为空");
        }

        for (Long configId : configIds) {
            deleteGlobalConfig(configId);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGlobalConfigStatus(Long configId, Integer status) {
        if (configId == null) {
            throw new BusinessException("配置ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效");
        }

        GlobalCommissionConfig config = this.getById(configId);
        if (config == null) {
            throw new BusinessException("全局佣金配置不存在");
        }

        // 如果要启用，检查同类型是否已有启用的配置
        if (status == 1) {
            LambdaQueryWrapper<GlobalCommissionConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GlobalCommissionConfig::getConfigName, config.getConfigName())
                   .eq(GlobalCommissionConfig::getStatus, 1)
                   .eq(GlobalCommissionConfig::getIsDeleted, 0)
                   .ne(GlobalCommissionConfig::getConfigId, configId);

            GlobalCommissionConfig existConfig = this.getOne(wrapper);
            if (existConfig != null) {
                throw new BusinessException("该用户类型已存在启用状态的全局佣金配置");
            }
        }

        config.setStatus(status);
        config.setUpdatedBy(AllContextUtils.getLoginSysUser().getUsername());
        config.setUpdatedAt(new Date());

        return this.updateById(config);
    }

    @Override
    public GlobalCommissionConfig getEffectiveConfigByUserType(Integer userType) {
        if (userType == null) {
            return null;
        }
        return baseMapper.selectEffectiveConfigByUserType(userType);
    }

    @Override
    public void validateGlobalConfig(GlobalCommissionConfigDTO dto) {
        if (dto == null) {
            throw new BusinessException("配置数据不能为空");
        }

        if (!StringUtils.hasText(dto.getConfigName())) {
            throw new BusinessException("配置名称不能为空");
        }

//        if (dto.getUserType() == null) {
//            throw new BusinessException("用户类型不能为空");
//        }
//
//        if (UserTypeEnum.getByCode(dto.getUserType()) == null) {
//            throw new BusinessException("用户类型无效");
//        }

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

//        if (dto.getStatus() == null || (dto.getStatus() != 0 && dto.getStatus() != 1)) {
//            throw new BusinessException("状态值无效");
//        }
    }
}
