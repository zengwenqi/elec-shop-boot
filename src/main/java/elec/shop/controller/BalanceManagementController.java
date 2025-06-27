package elec.shop.controller;

import elec.shop.pojo.purchase.FinanceAccount;
import elec.shop.service.purchase.AccountBalanceService;
import elec.shop.service.purchase.FinanceAccountService;
import elec.shop.sms.ExchangeRateService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Api(tags = "余额管理")
@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceManagementController {

    private final AccountBalanceService accountBalanceService;
    private final FinanceAccountService financeAccountService;
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/my")
    @ApiOperation("查询我的所有币种余额")
    public Result<Object> my() {
        // 获取当前登录用户
        Long userId = AllContextUtils.getLoginSysUser().getUserId();

        // 获取基础账户（人民币账户）
        FinanceAccount baseAccount = financeAccountService.getBaseAccount(userId);
        if (baseAccount == null) {
            return Result.fail().message("账户不存在");
        }

        // 获取所有外币余额
        Map<String, Object> balances = accountBalanceService.getAllBalances(baseAccount.getAccountId());

        // 添加人民币余额
        balances.put("CNY", baseAccount.getBanlance());

        return Result.ok(balances);
    }

    @PostMapping("/recharge")
    @ApiOperation("充值")
    public Result<Object> recharge(
            @ApiParam(value = "充值金额", required = true) @RequestParam BigDecimal amount,
            @ApiParam(value = "币种", required = true) @RequestParam String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail().message("充值金额必须大于0");
        }

        // 获取当前登录用户
        Long userId = AllContextUtils.getLoginSysUser().getUserId();

        try {
            boolean success;
            if ("CNY".equals(currency)) {
                // 人民币充值
                success = financeAccountService.rechargeCNY(userId, amount);
            } else {
                // 获取用户基础账户
                FinanceAccount baseAccount = financeAccountService.getBaseAccount(userId);
                if (baseAccount == null) {
                    return Result.fail().message("账户不存在");
                }
                // 外币充值
                success = accountBalanceService.rechargeForex(baseAccount.getAccountId(), currency, amount);
            }
            return success ? Result.ok(true) : Result.fail().message("充值失败");
        } catch (Exception e) {
            return Result.fail().message("充值失败：" + e.getMessage());
        }
    }

    @GetMapping("/exchange-rates")
    @ApiOperation("获取当前汇率")
    public Result<Object> getExchangeRates() {
        // 获取当前登录用户
        Long userId = AllContextUtils.getLoginSysUser().getUserId();

        // 获取基础账户
        FinanceAccount baseAccount = financeAccountService.getBaseAccount(userId);
        if (baseAccount == null) {
            return Result.fail().message("账户不存在");
        }

        // 获取所有币种汇率
        Map<String, BigDecimal> rates = accountBalanceService.getAllExchangeRates(baseAccount.getAccountId());
        return Result.ok(rates);
    }

    @PostMapping("/convert")
    @ApiOperation("币种转换")
    public Result<Object> convertCurrency(
            @ApiParam(value = "转出币种", required = true) @RequestParam String fromCurrency,
            @ApiParam(value = "转入币种", required = true) @RequestParam String toCurrency,
            @ApiParam(value = "转换金额", required = true) @RequestParam BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail().message("转换金额必须大于0");
        }

        // 获取当前登录用户
        Long userId = AllContextUtils.getLoginSysUser().getUserId();

        // 获取基础账户
        FinanceAccount baseAccount = financeAccountService.getBaseAccount(userId);
        if (baseAccount == null) {
            return Result.fail().message("账户不存在");
        }

        try {
            boolean success;
            if ("CNY".equals(fromCurrency)) {
                // 检查人民币余额是否足够
                if (!financeAccountService.checkCNYBalance(userId, amount)) {
                    return Result.fail().message("余额不足");
                }
                // 从人民币转换到其他币种
                success = financeAccountService.deductCNY(userId, amount) &&
                         accountBalanceService.rechargeForex(baseAccount.getAccountId(), toCurrency, amount);
            } else if ("CNY".equals(toCurrency)) {
                // 从其他币种转换到人民币
                Map<String, BigDecimal> rates = accountBalanceService.getAllExchangeRates(baseAccount.getAccountId());
                BigDecimal rate = rates.get(fromCurrency);
                if (rate == null) {
                    return Result.fail().message("不支持的币种转换");
                }

                success = accountBalanceService.convertCurrency(baseAccount.getAccountId(), fromCurrency, toCurrency,
                                                             amount, rate, BigDecimal.ONE);
            } else {
                // 其他币种之间的转换
                Map<String, BigDecimal> rates = accountBalanceService.getAllExchangeRates(baseAccount.getAccountId());
                BigDecimal sourceRate = rates.get(fromCurrency);
                BigDecimal targetRate = rates.get(toCurrency);
                if (sourceRate == null || targetRate == null) {
                    return Result.fail().message("不支持的币种转换");
                }

                success = accountBalanceService.convertCurrency(baseAccount.getAccountId(), fromCurrency, toCurrency,
                                                             amount, sourceRate, targetRate);
            }

            return success ? Result.ok(true) : Result.fail().message("币种转换失败");
        } catch (Exception e) {
            return Result.fail().message("币种转换失败：" + e.getMessage());
        }
    }
}
