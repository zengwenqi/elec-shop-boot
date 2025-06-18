package elec.shop.service.purchase;

import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.FinanceAccount;
import java.math.BigDecimal;

/**
* @author Lenovo
* @description 针对表【finance_account(账户信息表)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface FinanceAccountService extends IService<FinanceAccount> {
    
    /**
     * 获取用户基础账户（人民币账户）
     */
    FinanceAccount getBaseAccount(Long userId);
    
    /**
     * 人民币充值
     */
    boolean rechargeCNY(Long userId, BigDecimal amount);
    
    /**
     * 人民币扣减
     */
    boolean deductCNY(Long userId, BigDecimal amount);
    
    /**
     * 检查人民币余额是否足够
     */
    boolean checkCNYBalance(Long userId, BigDecimal amount);
}
