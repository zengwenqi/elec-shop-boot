package elec.shop.service.purchase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import elec.shop.exception.BusinessException;
import elec.shop.mapper.purchase.FinanceAccountMapper;
import elec.shop.mapper.sys.SysUserMapper;
import elec.shop.pojo.balance.dto.AccountBalanceDTO;
import elec.shop.pojo.purchase.AccountBalance;
import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.pojo.purchase.dto.CurrencyAccountBalanceDTO;
import elec.shop.pojo.purchase.dto.ExchangeRateDTO;
import elec.shop.pojo.purchase.enums.AccountStatusEnum;
import elec.shop.pojo.sys.SysUser;
import elec.shop.service.purchase.AccountBalanceService;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.service.sys.SysUserService;
import elec.shop.mapper.purchase.AccountBalanceMapper;
import elec.shop.pojo.purchase.vo.CurrencyAccountVO;
import elec.shop.sms.ExchangeRateService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.RsaDecryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
@Slf4j
public class AccountBalanceServiceImpl extends ServiceImpl<AccountBalanceMapper, AccountBalance>
    implements AccountBalanceService{

    private final FinanceAccountService financeAccountService;
    private final SysUserService sysUserService;
    private final AccountBalanceMapper accountBalanceMapper;
    private final FinanceAccountMapper financeAccountMapper;
    private final ExchangeRateService exchangeRateService;
    private final SysUserMapper sysUserMapper;

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

    @Override
    @Transactional
    public Boolean changeCurrencyAccounts(ExchangeRateDTO exchangeRateDTO) {
        AccountBalance accountBalance = new AccountBalance();
        BeanUtils.copyProperties(exchangeRateDTO, accountBalance);
        int update = accountBalanceMapper.update(accountBalance, new LambdaQueryWrapper<AccountBalance>()
                .eq(AccountBalance::getAccountId, exchangeRateDTO.getAccountId())
                .eq(AccountBalance::getCurrency, exchangeRateDTO.getCurrency()));
        if (update > 0)
            return true;
        return false;
    }

    @Override
    @Transactional
    public Boolean changeCurrencyAccountBalance(CurrencyAccountBalanceDTO currencyAccountBalanceDTO) {
        // 参数校验
        if (currencyAccountBalanceDTO == null ||
                StringUtils.isBlank(currencyAccountBalanceDTO.getAccountNo()) ||
                currencyAccountBalanceDTO.getBalance() == null ||
                currencyAccountBalanceDTO.getBalance().compareTo(BigDecimal.ZERO) <= 0 ||
                !Arrays.asList(1, 2).contains(currencyAccountBalanceDTO.getOperationStatus())) {
            throw new IllegalArgumentException("账户余额操作参数错误");
        }

        // 获取账户信息
        FinanceAccount account = financeAccountMapper.selectOne(new LambdaQueryWrapper<FinanceAccount>()
                .eq(FinanceAccount::getAccountNo,currencyAccountBalanceDTO.getAccountNo()));
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 检查账户状态
        if (!AccountStatusEnum.NORMAL.getCode().equals(account.getStatus())) {
            throw new BusinessException("账户状态异常，无法操作");
        }

        // 检查币种是否匹配
        if (!currencyAccountBalanceDTO.getCurrency().equals(account.getBaseCurrency())) {
            throw new BusinessException("币种不匹配");
        }

        // 构建更新条件和更新内容
        LambdaUpdateWrapper<FinanceAccount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FinanceAccount::getAccountNo, currencyAccountBalanceDTO.getAccountNo())
                .eq(FinanceAccount::getStatus, AccountStatusEnum.NORMAL.getCode());

        FinanceAccount updateEntity = new FinanceAccount();

        // 增加余额操作
        if (currencyAccountBalanceDTO.getOperationStatus() == 1) {
            updateEntity.setBanlance(account.getBanlance().add(currencyAccountBalanceDTO.getBalance()));
        }
        // 减少余额操作（需要检查余额是否充足）
        else if (currencyAccountBalanceDTO.getOperationStatus() == 2) {
            if (account.getBanlance().compareTo(currencyAccountBalanceDTO.getBalance()) < 0) {
                throw new BusinessException("余额不足");
            }
            updateEntity.setBanlance(account.getBanlance().subtract(currencyAccountBalanceDTO.getBalance()));
        }

        // 执行更新（使用CAS机制确保原子性）
        int rows = financeAccountMapper.update(updateEntity, updateWrapper);

        // 返回操作结果
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeCurrencyCNYAccountBalance(CurrencyAccountBalanceDTO dto) {
        // 参数校验
        validateRequest(dto);
        if (StringUtils.isBlank(dto.getAccountNo())){
            SysUser loginSysUser = AllContextUtils.getLoginSysUser();
            FinanceAccount baseAccount = financeAccountService.getBaseAccount(loginSysUser.getUserId());
            dto.setAccountNo(baseAccount.getAccountNo());
        }
        // 根据accountNo查找主账户(人民币)
        FinanceAccount mainAccount = getMainAccountByAccountNo(dto.getAccountNo());

        // 目标货币账户
        AccountBalance targetCurrencyAccount = getOrCreateTargetCurrencyAccount(
                mainAccount.getAccountId(),
                dto.getCurrency()
        );

        // 获取最新汇率
        BigDecimal exchangeRate = targetCurrencyAccount.getExchangeRate();
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            // 如果账户中没有汇率，从汇率服务获取
            exchangeRate = exchangeRateService.getExchangeRate(dto.getCurrency());
            if (exchangeRate == null) {
                throw new BusinessException("未找到有效汇率信息");
            }
            // 更新账户汇率
            targetCurrencyAccount.setExchangeRate(exchangeRate);
            accountBalanceMapper.updateById(targetCurrencyAccount);
        }

        // 计算转换金额
        BigDecimal cnyAmount = calculateCNYAmount(dto.getBalance(), exchangeRate, dto.getOperationStatus());

        // 执行账户余额变更
        if (dto.getOperationStatus() == 1) {
            // 人民币兑换目标货币：人民币减少，目标货币增加
            return convertCNYToTargetCurrency(mainAccount, targetCurrencyAccount, cnyAmount, dto.getBalance());
        } else {
            // 目标货币兑换人民币：人民币增加，目标货币减少
            return convertTargetCurrencyToCNY(mainAccount, targetCurrencyAccount, cnyAmount, dto.getBalance());
        }
    }

    @Override
    @Transactional
    public boolean increaseAccount(AccountBalanceDTO dto) throws Exception {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId, loginSysUser.getUserId()));
        if (sysUser.getUserType()!=1) return false;
        FinanceAccount mainAccount = financeAccountMapper.selectOne(new LambdaQueryWrapper<FinanceAccount>()
                .eq(FinanceAccount::getUserId,dto.getUserId()));
        if (mainAccount!=null) {
            mainAccount.setBanlance(mainAccount.getBanlance().add(RsaDecryptUtil.decryptAmount(dto.getMoney())));
            financeAccountMapper.updateById(mainAccount);
        }
        return true;
    }

    /**
     * 参数校验
     */
    private void validateRequest(CurrencyAccountBalanceDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }

//        if (StringUtils.isBlank(dto.getAccountNo())) {
//            throw new IllegalArgumentException("账户编号不能为空");
//        }

        if (dto.getBalance() == null || dto.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于0");
        }

        if (!Arrays.asList("CNY", "USD", "EUR", "GBP", "JPY").contains(dto.getCurrency())) {
            throw new IllegalArgumentException("不支持的货币类型");
        }

        if (!Arrays.asList(1, 2).contains(dto.getOperationStatus())) {
            throw new IllegalArgumentException("未知操作类型");
        }
    }

    /**
     * 根据账户编号获取主账户
     */
    private FinanceAccount getMainAccountByAccountNo(String accountNo) {
        LambdaQueryWrapper<FinanceAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FinanceAccount::getAccountNo, accountNo);
        FinanceAccount account = financeAccountMapper.selectOne(queryWrapper);

        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        if (!"CNY".equals(account.getBaseCurrency())) {
            throw new BusinessException("该账户不是人民币账户");
        }

        if (!AccountStatusEnum.NORMAL.getCode().equals(account.getStatus())) {
            throw new BusinessException("账户状态异常，无法操作");
        }

        return account;
    }

    /**
     * 获取目标货币账户
     */
    private AccountBalance getOrCreateTargetCurrencyAccount(String accountId, String currency) {
        // 查询是否已存在该币种账户
        LambdaQueryWrapper<AccountBalance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountBalance::getAccountId, accountId)
                .eq(AccountBalance::getCurrency, currency);
        AccountBalance account = accountBalanceMapper.selectOne(queryWrapper);
        return account;
    }

    /**
     * 计算人民币金额
     */
    private BigDecimal calculateCNYAmount(BigDecimal targetAmount,
                                          BigDecimal exchangeRate,
                                          Integer operationType) {
        if (operationType == 1) {
            // 人民币兑换目标货币：CNY金额 = 目标金额 / 汇率
            return targetAmount.divide(exchangeRate, 8, RoundingMode.HALF_UP);
        } else {
            // 目标货币兑换人民币：CNY金额 = 目标金额 * 汇率
            return targetAmount.multiply(exchangeRate);
        }
    }

    /**
     * 人民币兑换目标货币
     */
    private Boolean convertCNYToTargetCurrency(FinanceAccount mainAccount,
                                               AccountBalance targetAccount,
                                               BigDecimal cnyAmount,
                                               BigDecimal targetAmount) {
        // 检查人民币账户余额是否充足
        if (mainAccount.getBanlance().compareTo(targetAmount) < 0) {
            throw new BusinessException("人民币余额不足");
        }

        // 更新人民币账户余额
        LambdaUpdateWrapper<FinanceAccount> mainUpdateWrapper = new LambdaUpdateWrapper<>();
        mainUpdateWrapper.eq(FinanceAccount::getAccountId, mainAccount.getAccountId())
                .eq(FinanceAccount::getBanlance, mainAccount.getBanlance());

        FinanceAccount mainUpdate = new FinanceAccount();
        mainUpdate.setBanlance(mainAccount.getBanlance().subtract(targetAmount));

        int mainRows = financeAccountMapper.update(mainUpdate, mainUpdateWrapper);

        // 更新目标货币账户余额
        LambdaUpdateWrapper<AccountBalance> targetUpdateWrapper = new LambdaUpdateWrapper<>();
        targetUpdateWrapper.eq(AccountBalance::getBalanceId, targetAccount.getBalanceId())
                .eq(AccountBalance::getBalance, targetAccount.getBalance());

        AccountBalance targetUpdate = new AccountBalance();
        targetUpdate.setBalance(targetAccount.getBalance().add(cnyAmount));
        targetUpdate.setLastUpdated(new Date());

        int targetRows = accountBalanceMapper.update(targetUpdate, targetUpdateWrapper);

        // 记录交易日志
        recordTransactionLog(mainAccount.getAccountId(),
                mainAccount.getBaseCurrency(),
                targetAccount.getCurrency(),
                cnyAmount.negate(),
                targetAmount);

        return mainRows > 0 && targetRows > 0;
    }

    /**
     * 目标货币兑换人民币
     */
    private Boolean convertTargetCurrencyToCNY(FinanceAccount mainAccount,
                                               AccountBalance targetAccount,
                                               BigDecimal cnyAmount,
                                               BigDecimal targetAmount) {
        // 检查目标货币账户余额是否充足
        if (targetAccount.getBalance().compareTo(targetAmount) < 0) {
            throw new BusinessException("目标货币余额不足");
        }

        // 更新目标货币账户余额
        LambdaUpdateWrapper<AccountBalance> targetUpdateWrapper = new LambdaUpdateWrapper<>();
        targetUpdateWrapper.eq(AccountBalance::getBalanceId, targetAccount.getBalanceId())
                .eq(AccountBalance::getBalance, targetAccount.getBalance());

        AccountBalance targetUpdate = new AccountBalance();
        targetUpdate.setBalance(targetAccount.getBalance().subtract(targetAmount));
        targetUpdate.setLastUpdated(new Date());

        int targetRows = accountBalanceMapper.update(targetUpdate, targetUpdateWrapper);

        // 更新人民币账户余额
        LambdaUpdateWrapper<FinanceAccount> mainUpdateWrapper = new LambdaUpdateWrapper<>();
        mainUpdateWrapper.eq(FinanceAccount::getAccountId, mainAccount.getAccountId())
                .eq(FinanceAccount::getBanlance, mainAccount.getBanlance());

        FinanceAccount mainUpdate = new FinanceAccount();
        mainUpdate.setBanlance(mainAccount.getBanlance().add(cnyAmount));

        int mainRows = financeAccountMapper.update(mainUpdate, mainUpdateWrapper);

        // 记录交易日志
        recordTransactionLog(mainAccount.getAccountId(),
                targetAccount.getCurrency(),
                mainAccount.getBaseCurrency(),
                targetAmount.negate(),
                cnyAmount);

        return mainRows > 0 && targetRows > 0;
    }

    /**
     * 记录交易日志
     */
    private void recordTransactionLog(String accountId,
                                      String fromCurrency,
                                      String toCurrency,
                                      BigDecimal fromAmount,
                                      BigDecimal toAmount) {
        // 实现交易日志记录逻辑
        // 可以考虑记录：交易ID、账户ID、币种、金额、交易时间、交易类型等信息
    }
}




