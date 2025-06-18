package elec.shop.sms;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExchangeRateService {
    /**
     * 获取指定货币对人民币的汇率
     * @param currency 货币代码 (USD, EUR, GBP, JPY)
     * @return 汇率
     */
    BigDecimal getExchangeRate(String currency);
    
    /**
     * 获取主要货币的实时汇率和变化率
     * @return 汇率信息，包含当前汇率和变化率
     */
    Map<String, Object> getMainCurrencyRates();

    /**
     * 获取指定货币的历史汇率记录
     * @param currency 货币代码 (USD, EUR, GBP, JPY)
     * @return 历史汇率记录列表
     */
    List<Map<String, Object>> getCurrencyHistory(String currency);
}
