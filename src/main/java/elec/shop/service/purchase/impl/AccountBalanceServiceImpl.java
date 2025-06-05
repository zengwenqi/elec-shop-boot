package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.service.purchase.AccountBalanceService;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【account_balance(账户余额表（多币种）)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
public class AccountBalanceServiceImpl extends ServiceImpl<AccountBalanceMapper, AccountBalance>
    implements AccountBalanceService{

}




