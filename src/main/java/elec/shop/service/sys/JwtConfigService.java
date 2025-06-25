package elec.shop.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.sys.JwtConfigEntity;

public interface JwtConfigService extends IService<JwtConfigEntity> {
    String getConfigValue(String configKey);
    void updateConfig(String configKey, String configValue);
} 