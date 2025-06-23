package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.balance.ScheduledTask;
import elec.shop.service.balance.ScheduledTaskService;
import elec.shop.mapper.balance.ScheduledTaskMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【scheduled_task(定时任务表)】的数据库操作Service实现
* @createDate 2025-06-23 16:45:35
*/
@Service
public class ScheduledTaskServiceImpl extends ServiceImpl<ScheduledTaskMapper, ScheduledTask>
    implements ScheduledTaskService{

}




