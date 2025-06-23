package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.balance.ServiceTicket;
import elec.shop.service.balance.ServiceTicketService;
import elec.shop.mapper.balance.ServiceTicketMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【service_ticket(工单表)】的数据库操作Service实现
* @createDate 2025-06-23 16:45:35
*/
@Service
public class ServiceTicketServiceImpl extends ServiceImpl<ServiceTicketMapper, ServiceTicket>
    implements ServiceTicketService{

}




