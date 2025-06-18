package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.AccountBalanceService;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.service.sys.SysUserService;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.pojo.purchase.vo.CurrencyAccountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【account_balance(账户余额表（多币种）)】的数据库操作Service实现
* @createDate 2025-06-05 11:28:32
*/
@Service
@RequiredArgsConstructor
public class AccountBalanceServiceImpl extends ServiceImpl<AccountBalanceMapper, AccountBalance>
    implements AccountBalanceService{

    private final FinanceAccountService financeAccountService;
    private final SysUserService sysUserService;

    @Override
    public Map<String, Object> getAllBalances(String accountId) {
        // 查询该账户下所有外币余额
        List<AccountBalance> balances = this.lambdaQuery()
                .eq(AccountBalance::getAccountId, accountId)
                .list();

        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        for (AccountBalance balance : balances) {
            result.put(balance.getCurrency(), balance.getBalance());
            result.put(balance.getCurrency() + "_rate", balance.getExchangeRate());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeForex(String accountId, String currency, BigDecimal amount) {
        AccountBalance accountBalance = this.lambdaQuery()
                .eq(AccountBalance::getAccountId, accountId)
                .eq(AccountBalance::getCurrency, currency)
                .one();

        if (accountBalance == null) {
            return false;
        }

        // 更新余额
        accountBalance.setBalance(accountBalance.getBalance().add(amount));
        return this.updateById(accountBalance);
    }

    @Override
    public Map<String, BigDecimal> getAllExchangeRates(String accountId) {
        List<AccountBalance> balances = this.lambdaQuery()
                .eq(AccountBalance::getAccountId, accountId)
                .list();

        Map<String, BigDecimal> rates = new HashMap<>();
        for (AccountBalance balance : balances) {
            rates.put(balance.getCurrency(), balance.getExchangeRate());
        }

        return rates;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean convertCurrency(String accountId, String fromCurrency, String toCurrency,
                                 BigDecimal amount, BigDecimal sourceRate, BigDecimal targetRate) {
        // 计算转换金额
        BigDecimal convertedAmount = amount.multiply(sourceRate).divide(targetRate, 2, BigDecimal.ROUND_DOWN);

        // 获取源币种账户
        AccountBalance sourceAccount = this.lambdaQuery()
                .eq(AccountBalance::getAccountId, accountId)
                .eq(AccountBalance::getCurrency, fromCurrency)
                .one();

        // 获取目标币种账户
        AccountBalance targetAccount = this.lambdaQuery()
                .eq(AccountBalance::getAccountId, accountId)
                .eq(AccountBalance::getCurrency, toCurrency)
                .one();

        if (sourceAccount == null || targetAccount == null) {
            return false;
        }

        // 检查余额是否足够
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // 执行转换
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(convertedAmount));

        // 批量更新
        return this.updateBatchById(List.of(sourceAccount, targetAccount));
    }

    @Override
    public IPage<CurrencyAccountVO> getCurrencyAccounts(String currency, Page<CurrencyAccountVO> page) {
        Page<CurrencyAccountVO> resultPage = new Page<>(page.getCurrent(), page.getSize());
        List<CurrencyAccountVO> records = new ArrayList<>();

        if ("CNY".equals(currency)) {
            // 查询人民币账户（分页）
            Page<FinanceAccount> accountPage = new Page<>(page.getCurrent(), page.getSize());
            IPage<FinanceAccount> accountResult = financeAccountService.page(accountPage);

            // 获取所有用户ID
            Set<Long> userIds = accountResult.getRecords().stream()
                    .map(FinanceAccount::getUserId)
                    .collect(Collectors.toSet());

            // 批量查询用户信息
            Map<Long, SysUser> userMap = sysUserService.listByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));

            records = accountResult.getRecords().stream()
                    .map(account -> {
                        CurrencyAccountVO vo = new CurrencyAccountVO();
                        vo.setUserId(account.getUserId());
                        vo.setAccountId(account.getAccountId());
                        vo.setAccountNo(account.getAccountNo());
                        vo.setBalance(account.getBanlance());
                        vo.setCurrency("CNY");
                        vo.setAccountType(account.getAccountType());
                        vo.setStatus(account.getStatus());

                        // 填充用户信息
                        SysUser user = userMap.get(account.getUserId());
                        if (user != null) {
                            vo.setUsername(user.getUsername());
                            vo.setRealName(user.getRealName());
                            vo.setEmail(user.getEmail());
                            vo.setAvatar(user.getAvatar());
                            vo.setUserType(user.getUserType());
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());

            resultPage.setTotal(accountResult.getTotal());
        } else {
            // 查询外币账户（分页）
            Page<AccountBalance> balancePage = new Page<>(page.getCurrent(), page.getSize());
            IPage<AccountBalance> balanceResult = this.lambdaQuery()
                    .eq(AccountBalance::getCurrency, currency)
                    .orderByDesc(AccountBalance::getBalance)
                    .page(balancePage);

            // 获取账户基本信息
            Set<String> accountIds = balanceResult.getRecords().stream()
                    .map(AccountBalance::getAccountId)
                    .collect(Collectors.toSet());

            Map<String, FinanceAccount> accountMap = financeAccountService.lambdaQuery()
                    .in(FinanceAccount::getAccountId, accountIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(
                            FinanceAccount::getAccountId,
                            account -> account
                    ));

            // 获取所有用户ID
            Set<Long> userIds = accountMap.values().stream()
                    .map(FinanceAccount::getUserId)
                    .collect(Collectors.toSet());

            // 批量查询用户信息
            Map<Long, SysUser> userMap = sysUserService.listByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));

            records = balanceResult.getRecords().stream()
                    .map(balance -> {
                        CurrencyAccountVO vo = new CurrencyAccountVO();
                        FinanceAccount account = accountMap.get(balance.getAccountId());
                        if (account != null) {
                            vo.setUserId(account.getUserId());
                            vo.setAccountId(balance.getAccountId());
                            vo.setAccountNo(account.getAccountNo());
                            vo.setBalance(balance.getBalance());
                            vo.setCurrency(balance.getCurrency());
                            vo.setSymbol(balance.getSymbol());
                            vo.setExchangeRate(balance.getExchangeRate());
                            vo.setAccountType(account.getAccountType());
                            vo.setStatus(account.getStatus());
                            vo.setLastUpdated(balance.getLastUpdated());

                            // 填充用户信息
                            SysUser user = userMap.get(account.getUserId());
                            if (user != null) {
                                vo.setUsername(user.getUsername());
                                vo.setRealName(user.getRealName());
                                vo.setEmail(user.getEmail());
                                vo.setAvatar(user.getAvatar());
                                vo.setUserType(user.getUserType());
                            }
                        }
                        return vo;
                    })
                    .filter(vo -> vo.getUserId() != null)
                    .collect(Collectors.toList());

            resultPage.setTotal(balanceResult.getTotal());
        }

        resultPage.setRecords(records);
        return resultPage;
    }
}




