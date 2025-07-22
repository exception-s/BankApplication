package com.BankApp.localbankapp.mapper;


import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
@Component
public class AccountMapper {
    public static BankAccount toEntity(AccountDTO dto, User user, String generatedAccountNumber) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setCurrency(dto.getCurrency());
        account.setAccountNumber(generatedAccountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);
        return account;
    }
}