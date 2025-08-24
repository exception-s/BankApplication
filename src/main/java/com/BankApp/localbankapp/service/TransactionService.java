package com.BankApp.localbankapp.service;

import com.BankApp.localbankapp.model.Transaction;
import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
public interface TransactionService {
    Transaction transfer(Long fromId, Long toId, BigDecimal amount);
}