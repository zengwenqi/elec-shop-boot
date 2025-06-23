package elec.shop.service.balance.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.balance.FinanceTransaction;
import elec.shop.service.balance.FinanceTransactionService;
import elec.shop.mapper.balance.FinanceTransactionMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【finance_transaction(交易流水表)】的数据库操作Service实现
* @createDate 2025-06-23 16:45:35
*/
@Service
public class FinanceTransactionServiceImpl extends ServiceImpl<FinanceTransactionMapper, FinanceTransaction>
    implements FinanceTransactionService{

}




