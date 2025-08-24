package com.BankApp.localbankapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Alexander Brazhkin
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Account not found with ID: " + id);
    }

    public AccountNotFoundException(String username) {
        super("Account not found with username: " + username);
    }
}