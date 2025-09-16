package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.util.CurrencyConverter;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alexander Brazhkin
 */
class CurrencyConverterTest {
    private static CurrencyConverter converter;

    @BeforeAll
    static void init() {
        converter = new CurrencyConverter();
    }

    @Test
    @DisplayName("USD to EUR")
    void testConvertUsdToEur() {
        BigDecimal amount = new BigDecimal("100");
        BigDecimal result = converter.convert(amount, "USD", "EUR");

        System.out.println("Converted amount: " + result);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("USD to RUB")
    void testConvertUsdToRub() {
        BigDecimal amount = new BigDecimal("100");
        BigDecimal result = converter.convert(amount, "USD", "RUB");

        System.out.println("Converted amount: " + result);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("EUR to EUR")
    void testConvertSameCurrency() {
        BigDecimal amount = new BigDecimal("50");
        BigDecimal result = converter.convert(amount, "EUR", "EUR");

        assertEquals(amount.setScale(2, RoundingMode.HALF_UP), result);
    }
}