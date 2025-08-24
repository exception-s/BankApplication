package com.BankApp.localbankapp.service;

import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.model.BankAccount;

/**
 * @author Alexander Brazhkin
 */
public interface AccountService {
    BankAccount createAccount(AccountDTO dto);
    BankAccount getAccountById(Long id);
}