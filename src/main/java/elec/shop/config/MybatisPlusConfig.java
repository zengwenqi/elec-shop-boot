package elec.shop.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan("elec.shop.mapper")
@Configuration
public class MybatisPlusConfig {
}
