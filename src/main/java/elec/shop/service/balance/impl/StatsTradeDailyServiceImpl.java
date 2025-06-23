package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.balance.StatsTradeDaily;
import elec.shop.service.balance.StatsTradeDailyService;
import elec.shop.mapper.balance.StatsTradeDailyMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【stats_trade_daily(交易统计表（按天）)】的数据库操作Service实现
* @createDate 2025-06-23 16:45:35
*/
@Service
public class StatsTradeDailyServiceImpl extends ServiceImpl<StatsTradeDailyMapper, StatsTradeDaily>
    implements StatsTradeDailyService{

}




