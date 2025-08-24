package com.BankApp.localbankapp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CurrencyConverter {
    private static final String CBR_URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        try {
            String xml = restTemplate.getForObject(CBR_URL, String.class);
            assert xml != null;
            Map<String, BigDecimal> rates = parseXmlRates(xml);

            BigDecimal rubToFrom = fromCurrency.equalsIgnoreCase("RUB") ? BigDecimal.ONE : rates.get(fromCurrency.toUpperCase());
            BigDecimal rubToTo = toCurrency.equalsIgnoreCase("RUB") ? BigDecimal.ONE : rates.get(toCurrency.toUpperCase());

            if (rubToFrom == null || rubToTo == null) {
                throw new IllegalArgumentException("Unsupported currency: " + fromCurrency + " or " + toCurrency);
            }

            BigDecimal inRubles = fromCurrency.equalsIgnoreCase("RUB") ? amount : amount.multiply(rubToFrom);
            BigDecimal result = toCurrency.equalsIgnoreCase("RUB") ? inRubles : inRubles.divide(rubToTo, 2, RoundingMode.HALF_UP);
            return result.setScale(3, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("Currency conversion failed: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch exchange rates", e);
        }
    }

    private Map<String, BigDecimal> parseXmlRates(String xml) throws Exception {
        Map<String, BigDecimal> rates = new HashMap<>();
        Document doc = DocumentBuilderFactory.newInstance()
                                             .newDocumentBuilder()
                                             .parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        NodeList valutes = doc.getElementsByTagName("Valute");

        for (int i = 0; i < valutes.getLength(); i++) {
            Element valute = (Element) valutes.item(i);
            String code = valute.getElementsByTagName("CharCode").item(0).getTextContent();
            String valueStr = valute.getElementsByTagName("Value").item(0).getTextContent();
            String nominalStr = valute.getElementsByTagName("Nominal").item(0).getTextContent();

            BigDecimal value = new BigDecimal(valueStr.replace(",", "."));
            BigDecimal nominal = new BigDecimal(nominalStr);
            BigDecimal rate = value.divide(nominal, 6, RoundingMode.HALF_UP);

            rates.put(code.toUpperCase(), rate);
        }

        return rates;
    }
}
