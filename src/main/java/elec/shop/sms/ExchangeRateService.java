package elec.shop.sms;

import java.math.BigDecimal;

public interface ExchangeRateService {
    /**
     * 获取指定货币对人民币的汇率
     * @param currency 货币代码 (USD, EUR, GBP, JPY)
     * @return 汇率
     */
    BigDecimal getExchangeRate(String currency);
}
