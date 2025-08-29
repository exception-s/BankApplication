package com.BankApp.localbankapp.service;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.model.Transaction;

/**
 * @author Alexander Brazhkin
 */
public interface TransactionService {
    Transaction transfer(TransactionDTO dto);
    Transaction deposit(TransactionDTO dto);
    Transaction withdrawal(TransactionDTO dto);
}