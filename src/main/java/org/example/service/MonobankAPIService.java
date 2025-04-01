package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonobankAPIService {

    private static final String MONOBANK_API_URL = "https://api.monobank.ua/bank/currency";
    private static final int USD_CODE = 840;
    private static final int EUR_CODE = 978;
    private static final int UAH_CODE = 980;

    private Map<String, BigDecimal> exchangeRates = new HashMap<>();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 3600000;

    public String getRawExchangeRates() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject(MONOBANK_API_URL, String.class);
        } catch (Exception e) {
            System.err.println("Помилка при запиті до API Монобанку: " + e.getMessage());
            return "[]";
        }
    }

    public Map<String, BigDecimal> getExchangeRates() {
        long currentTime = System.currentTimeMillis();

        if (exchangeRates.isEmpty() || currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            System.out.println("Оновлення курсів валют...");
            updateExchangeRates();
            lastUpdateTime = currentTime;
            System.out.println("Поточні курси валют: " + exchangeRates);
        }

        return exchangeRates;
    }

    private void updateExchangeRates() {
        String rawData = getRawExchangeRates();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            List<CurrencyRate> rates = mapper.readValue(rawData, new TypeReference<List<CurrencyRate>>() {});

            if (rates.isEmpty()) {
                setDefaultRates();
                return;
            }

            boolean usdFound = false;
            boolean eurFound = false;

            for (CurrencyRate rate : rates) {
                if (rate.getCurrencyCodeB() == UAH_CODE) {
                    if (rate.getCurrencyCodeA() == USD_CODE) {
                        exchangeRates.put("USD", rate.getRateBuy());
                        usdFound = true;
                    } else if (rate.getCurrencyCodeA() == EUR_CODE) {
                        exchangeRates.put("EUR", rate.getRateBuy());
                        eurFound = true;
                    }
                }
            }

            if (!usdFound) {
                exchangeRates.put("USD", BigDecimal.valueOf(38.0));
            }
            if (!eurFound) {
                exchangeRates.put("EUR", BigDecimal.valueOf(41.0));
            }

        } catch (JsonProcessingException e) {
            System.err.println("Помилка при обробці даних про курси валют: " + e.getMessage());
            e.printStackTrace();
            setDefaultRates();
        }
    }

    private void setDefaultRates() {
        exchangeRates.put("USD", BigDecimal.valueOf(38.0));
        exchangeRates.put("EUR", BigDecimal.valueOf(41.0));
    }

    public BigDecimal convertToUAH(BigDecimal amount, String fromCurrency) {
        if ("UAH".equals(fromCurrency)) {
            return amount;
        }

        Map<String, BigDecimal> rates = getExchangeRates();
        BigDecimal rate = rates.getOrDefault(fromCurrency, BigDecimal.ONE);

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private static class CurrencyRate {
        private int currencyCodeA;
        private int currencyCodeB;
        private BigDecimal rateBuy;

        public int getCurrencyCodeA() {
            return currencyCodeA;
        }

        public int getCurrencyCodeB() {
            return currencyCodeB;
        }

        public BigDecimal getRateBuy() {
            return rateBuy;
        }
    }
}
