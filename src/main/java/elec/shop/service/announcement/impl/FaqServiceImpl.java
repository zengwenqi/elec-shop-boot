package elec.shop.service.announcement.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.announcement.Faq;
import elec.shop.service.announcement.FaqService;
import elec.shop.mapper.announcement.FaqMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【faq(常见问题表)】的数据库操作Service实现
* @createDate 2025-06-19 14:19:39
*/
@Service
public class FaqServiceImpl extends ServiceImpl<FaqMapper, Faq>
    implements FaqService{

}




