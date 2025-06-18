package elec.shop.sms.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elec.shop.sms.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String EXCHANGE_RATE_URL = "https://www.chinamoney.com.cn/ags/ms/cm-u-bk-ccpr/CcprHisNew";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExchangeRateServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public BigDecimal getExchangeRate(String currency) {
        try {
            Map<String, Object> rates = getMainCurrencyRates();
            if (rates.containsKey(currency)) {
                Map<String, Object> currencyInfo = (Map<String, Object>) rates.get(currency);
                return (BigDecimal) currencyInfo.get("rate");
            }
        } catch (Exception e) {
            log.error("获取汇率失败", e);
        }
        return BigDecimal.ONE;
    }

    @Override
    public Map<String, Object> getMainCurrencyRates() {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = restTemplate.getForObject(EXCHANGE_RATE_URL, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode records = root.get("records");
            JsonNode head = root.get("data").get("head");

            if (records != null && records.isArray() && records.size() >= 2) {
                JsonNode todayRecord = records.get(0);
                JsonNode yesterdayRecord = records.get(1);
                JsonNode todayValues = todayRecord.get("values");
                JsonNode yesterdayValues = yesterdayRecord.get("values");
                String updateDate = todayRecord.get("date").asText();

                for (int i = 0; i < head.size(); i++) {
                    String currencyPair = head.get(i).asText();
                    switch (currencyPair) {
                        case "USD/CNY", "EUR/CNY", "GBP/CNY", "100JPY/CNY" -> {
                            BigDecimal todayRate = new BigDecimal(todayValues.get(i).asText());
                            BigDecimal yesterdayRate = new BigDecimal(yesterdayValues.get(i).asText());
                            
                            // 计算变化率
                            BigDecimal changeRate = todayRate.subtract(yesterdayRate)
                                    .divide(yesterdayRate, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal("100"));
                            
                            Map<String, Object> currencyInfo = new HashMap<>();
                            String currency = currencyPair.split("/")[0];
                            
                            // 处理日元特殊情况
                            if ("100JPY".equals(currency)) {
                                currency = "JPY";
                                todayRate = todayRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                            }
                            
                            currencyInfo.put("rate", todayRate);
                            currencyInfo.put("change", changeRate);
                            currencyInfo.put("updateTime", updateDate);
                            
                            result.put(currency, currencyInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取汇率数据失败", e);
            setDefaultRates(result);
        }
        return result;
    }

    private void setDefaultRates(Map<String, Object> result) {
        String[][] defaultRates = {
            // 币种, 今日汇率, 昨日汇率
            {"USD", "7.1761", "7.1746"},
            {"EUR", "8.2558", "8.2932"},
            {"GBP", "9.6580", "9.7423"},
            {"JPY", "0.049456", "0.049612"}
        };

        for (String[] rate : defaultRates) {
            BigDecimal todayRate = new BigDecimal(rate[1]);
            BigDecimal yesterdayRate = new BigDecimal(rate[2]);
            BigDecimal changeRate = todayRate.subtract(yesterdayRate)
                    .divide(yesterdayRate, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            Map<String, Object> currencyInfo = new HashMap<>();
            currencyInfo.put("rate", todayRate);
            currencyInfo.put("change", changeRate);
            currencyInfo.put("updateTime", new Date());

            result.put(rate[0], currencyInfo);
        }
    }

    @Override
    public List<Map<String, Object>> getCurrencyHistory(String currency) {
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            String response = restTemplate.getForObject(EXCHANGE_RATE_URL, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode records = root.get("records");
            JsonNode head = root.get("data").get("head");

            if (records != null && records.isArray()) {
                // 找到对应货币的索引
                int currencyIndex = -1;
                String targetPair = currency.equals("JPY") ? "100JPY/CNY" : currency + "/CNY";
                
                for (int i = 0; i < head.size(); i++) {
                    if (head.get(i).asText().equals(targetPair)) {
                        currencyIndex = i;
                        break;
                    }
                }

                if (currencyIndex != -1) {
                    // 遍历所有记录
                    for (int i = 0; i < records.size(); i++) {
                        JsonNode record = records.get(i);
                        String date = record.get("date").asText();
                        String rateStr = record.get("values").get(currencyIndex).asText();
                        BigDecimal rate = new BigDecimal(rateStr);
                        
                        // 如果是日元，需要除以100
                        if (currency.equals("JPY")) {
                            rate = rate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                        }
                        
                        // 计算变化率（相对于前一天）
                        BigDecimal changeRate = BigDecimal.ZERO;
                        if (i < records.size() - 1) {
                            BigDecimal previousRate = new BigDecimal(records.get(i + 1)
                                    .get("values").get(currencyIndex).asText());
                            if (currency.equals("JPY")) {
                                previousRate = previousRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                            }
                            changeRate = rate.subtract(previousRate)
                                    .divide(previousRate, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal("100"));
                        }
                        
                        Map<String, Object> dataPoint = new HashMap<>();
                        dataPoint.put("date", date);
                        dataPoint.put("rate", rate);
                        dataPoint.put("change", changeRate);
                        
                        history.add(dataPoint);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取货币历史数据失败: " + currency, e);
            // 添加默认数据
            setDefaultHistory(history, currency);
        }
        return history;
    }

    private void setDefaultHistory(List<Map<String, Object>> history, String currency) {
        String[][] defaultData = {
            {"2025-06-18", "7.1761", "0.0209"},
            {"2025-06-17", "7.1746", "-0.0599"},
            {"2025-06-16", "7.1789", "0.0237"},
            {"2025-06-13", "7.1772", "-0.0431"}
        };

        for (String[] data : defaultData) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", data[0]);
            dataPoint.put("rate", new BigDecimal(data[1]));
            dataPoint.put("change", new BigDecimal(data[2]));
            history.add(dataPoint);
        }
    }
}
