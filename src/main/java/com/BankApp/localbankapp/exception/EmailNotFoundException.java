package com.BankApp.localbankapp.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author Alexander Brazhkin
 */
public class EmailNotFoundException extends AuthenticationException {
    public EmailNotFoundException(String msg) {
        super(msg);
    }

    public EmailNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
