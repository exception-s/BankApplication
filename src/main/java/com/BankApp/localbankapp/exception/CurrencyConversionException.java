package com.BankApp.localbankapp.exception;

/**
 * @author Alexander Brazhkin
 */
public class CurrencyConversionException extends RuntimeException {
    public CurrencyConversionException(String message) {
        super(message);
    }

    public CurrencyConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
