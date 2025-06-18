package elec.shop.service.purchase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.pojo.purchase.vo.CurrencyAccountVO;

import java.math.BigDecimal;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【account_balance(账户余额表（多币种）)】的数据库操作Service
* @createDate 2025-06-05 11:28:32
*/
public interface AccountBalanceService extends IService<AccountBalance> {

    /**
     * 获取指定账户下所有币种余额和汇率
     */
    Map<String, Object> getAllBalances(String accountId);

    /**
     * 外币充值
     */
    boolean rechargeForex(String accountId, String currency, BigDecimal amount);

    /**
     * 获取所有币种汇率
     */
    Map<String, BigDecimal> getAllExchangeRates(String accountId);

    /**
     * 币种转换
     */
    boolean convertCurrency(String accountId, String fromCurrency, String toCurrency,
                          BigDecimal amount, BigDecimal sourceRate, BigDecimal targetRate);

    /**
     * 获取指定货币的所有账户信息（分页）
     * @param currency 货币代码（CNY, USD, EUR, GBP, JPY）
     * @param page 分页参数
     * @return 分页的账户信息列表
     */
    IPage<CurrencyAccountVO> getCurrencyAccounts(String currency, Page<CurrencyAccountVO> page);
}
