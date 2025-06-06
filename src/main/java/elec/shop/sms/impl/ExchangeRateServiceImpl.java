package elec.shop.sms.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.sms.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String EXCHANGE_RATE_URL = "https://www.chinamoney.com.cn/ags/ms/cm-u-bk-ccpr/CcprHisNew";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private Map<String, BigDecimal> rateCache = new HashMap<>();

    public ExchangeRateServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        updateExchangeRates();
    }

    @Override
    public BigDecimal getExchangeRate(String currency) {
        if (rateCache.isEmpty()) {
            updateExchangeRates();
        }
        return rateCache.getOrDefault(currency, BigDecimal.ONE);
    }

    private void updateExchangeRates() {
        try {
            String response = restTemplate.getForObject(EXCHANGE_RATE_URL, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode records = root.get("records");

            if (records != null && records.isArray() && records.size() > 0) {
                JsonNode latestRecord = records.get(0);
                JsonNode values = latestRecord.get("values");
                JsonNode head = root.get("data").get("head");

                for (int i = 0; i < head.size(); i++) {
                    String currencyPair = head.get(i).asText();
                    String rate = values.get(i).asText();

                    switch (currencyPair) {
                        case "USD/CNY" -> rateCache.put("USD", new BigDecimal(rate));
                        case "EUR/CNY" -> rateCache.put("EUR", new BigDecimal(rate));
                        case "GBP/CNY" -> rateCache.put("GBP", new BigDecimal(rate));
                        case "100JPY/CNY" -> {
                            // 将100日元对人民币的汇率转换为1日元对人民币的汇率
                            BigDecimal jpyRate = new BigDecimal(rate).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                            rateCache.put("JPY", jpyRate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("更新汇率数据失败", e);
            // 设置默认汇率
            setDefaultRates();
        }
    }

    private void setDefaultRates() {
        rateCache.put("USD", new BigDecimal("7.1865"));
        rateCache.put("EUR", new BigDecimal("8.2045"));
        rateCache.put("GBP", new BigDecimal("9.7323"));
        rateCache.put("JPY", new BigDecimal("0.050352")); // 100日元 = 5.0352人民币
    }
}
