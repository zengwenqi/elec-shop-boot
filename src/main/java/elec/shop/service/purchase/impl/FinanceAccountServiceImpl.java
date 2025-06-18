package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
* @author Lenovo
* @description 针对表【finance_account(账户信息表)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
public class FinanceAccountServiceImpl extends ServiceImpl<FinanceAccountMapper, FinanceAccount>
    implements FinanceAccountService{

    @Override
    public FinanceAccount getBaseAccount(Long userId) {
        return this.lambdaQuery()
                .eq(FinanceAccount::getUserId, userId)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeCNY(Long userId, BigDecimal amount) {
        FinanceAccount account = getBaseAccount(userId);
        if (account == null) {
            return false;
        }
        
        // 更新人民币余额
        account.setBanlance(account.getBanlance().add(amount));
        return this.updateById(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductCNY(Long userId, BigDecimal amount) {
        FinanceAccount account = getBaseAccount(userId);
        if (account == null || !checkCNYBalance(userId, amount)) {
            return false;
        }
        
        // 扣减人民币余额
        account.setBanlance(account.getBanlance().subtract(amount));
        return this.updateById(account);
    }

    @Override
    public boolean checkCNYBalance(Long userId, BigDecimal amount) {
        FinanceAccount account = getBaseAccount(userId);
        return account != null && account.getBanlance().compareTo(amount) >= 0;
    }
}




