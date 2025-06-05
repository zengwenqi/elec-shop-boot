package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【finance_account(账户信息表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
public class FinanceAccountServiceImpl extends ServiceImpl<FinanceAccountMapper, FinanceAccount>
    implements FinanceAccountService{

}




