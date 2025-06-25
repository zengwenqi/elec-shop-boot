package elec.shop.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.mapper.sys.JwtConfigMapper;
import elec.shop.pojo.sys.JwtConfigEntity;
import elec.shop.service.sys.JwtConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtConfigServiceImpl extends ServiceImpl<JwtConfigMapper, JwtConfigEntity> implements JwtConfigService {

    @Override
    public String getConfigValue(String configKey) {
        JwtConfigEntity config = this.getOne(
            new LambdaQueryWrapper<JwtConfigEntity>()
                .eq(JwtConfigEntity::getConfigKey, configKey)
        );
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional
    public void updateConfig(String configKey, String configValue) {
        JwtConfigEntity config = this.getOne(
            new LambdaQueryWrapper<JwtConfigEntity>()
                .eq(JwtConfigEntity::getConfigKey, configKey)
        );
        
        if (config != null) {
            config.setConfigValue(configValue);
            this.updateById(config);
        } else {
            config = new JwtConfigEntity();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            this.save(config);
        }
    }
} 