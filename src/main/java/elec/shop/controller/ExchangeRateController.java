package elec.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import elec.shop.pojo.purchase.dto.CurrencyAccountBalanceDTO;
import elec.shop.pojo.purchase.dto.ExchangeRateDTO;
import elec.shop.service.purchase.AccountBalanceService;
import elec.shop.sms.ExchangeRateService;
import elec.shop.utils.Result;
import elec.shop.pojo.purchase.vo.CurrencyAccountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(tags = "汇率管理")
@RestController
@RequestMapping("/exchange-rat")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final AccountBalanceService accountBalanceService;

    @GetMapping("/real-time-rates")
    @ApiOperation("获取实时汇率")
    public Result<Object> getRealTimeRates() {
        try {
            Map<String, Object> rates = exchangeRateService.getMainCurrencyRates();
            return Result.ok(rates);
        } catch (Exception e) {
            return Result.fail().message("获取实时汇率失败：" + e.getMessage());
        }
    }

    @GetMapping("/currency-history/{currency}")
    @ApiOperation("获取指定货币的历史汇率")
    public Result<Object> getCurrencyHistory(
            @ApiParam(value = "货币代码", required = true, example = "USD")
            @PathVariable String currency) {
        try {
            if (!Arrays.asList("USD", "EUR", "GBP", "JPY").contains(currency)) {
                return Result.fail().message("不支持的货币类型");
            }
            List<Map<String, Object>> history = exchangeRateService.getCurrencyHistory(currency);
            return Result.ok(history);
        } catch (Exception e) {
            return Result.fail().message("获取历史汇率失败：" + e.getMessage());
        }
    }

    @GetMapping("/accounts/{currency}")
    @ApiOperation("获取指定货币的所有账户信息")
    public Result<Object> getCurrencyAccounts(
            @ApiParam(value = "货币代码", required = true, example = "USD")
            @PathVariable String currency,
            @ApiParam(value = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (!Arrays.asList("USD", "EUR", "GBP", "JPY", "CNY").contains(currency)) {
                return Result.fail().message("不支持的货币类型");
            }
            Page<CurrencyAccountVO> page = new Page<>(pageNum, pageSize);
            IPage<CurrencyAccountVO> result = accountBalanceService.getCurrencyAccounts(currency, page);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail().message("获取账户信息失败：" + e.getMessage());
        }
    }

    @PostMapping("/accounts/change/exchange-rate")
    @ApiOperation("更新指定账户的指定汇率信息")
    public Result<Object> changeCurrencyAccounts(@RequestBody ExchangeRateDTO exchangeRateDTO) {
        try {
            if (!Arrays.asList("USD", "EUR", "GBP", "JPY", "CNY").contains(exchangeRateDTO.getCurrency())) {
                return Result.fail().message("不支持的货币类型");
            }
            Boolean result = accountBalanceService.changeCurrencyAccounts(exchangeRateDTO);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail().message("获取账户信息失败：" + e.getMessage());
        }
    }

    @PostMapping("/accounts/change")
    @ApiOperation("更新指定账户的指定货币的余额")
    public Result<Object> changeCurrencyAccountBalance(@RequestBody CurrencyAccountBalanceDTO currencyAccountBalanceDTO) {
        try {
            if (!Arrays.asList("USD", "EUR", "GBP", "JPY", "CNY").contains(currencyAccountBalanceDTO.getCurrency())) {
                return Result.fail().message("不支持的货币类型");
            }
            Boolean result = accountBalanceService.changeCurrencyAccountBalance(currencyAccountBalanceDTO);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail().message("更新指定账户的余额失败：" + e.getMessage());
        }
    }

    @PostMapping("/accounts/CNY/change")
    @ApiOperation("账户的指定货币和人民币的转换(基于汇率)")
    public Result<Object> changeCurrencyCNYAccountBalance(@RequestBody CurrencyAccountBalanceDTO currencyAccountBalanceDTO) {
        try {
            if (!Arrays.asList("USD", "EUR", "GBP", "JPY", "CNY").contains(currencyAccountBalanceDTO.getCurrency())) {
                return Result.fail().message("不支持的货币类型");
            }
            Boolean result = accountBalanceService.changeCurrencyCNYAccountBalance(currencyAccountBalanceDTO);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail().message("账户货币转换失败：" + e.getMessage());
        }
    }
}
