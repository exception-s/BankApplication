package com.BankApp.localbankapp.mapper;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.Transaction;
import com.BankApp.localbankapp.model.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Alexander Brazhkin
 */
@Component
public class TransactionMapper {
    public static Transaction toEntity(TransactionDTO dto,
                                       BankAccount from,
                                       BankAccount to,
                                       TransactionType type,
                                       String description)
    {
        Transaction tx = new Transaction();
        tx.setAmount(dto.getAmount());
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setType(type);
        tx.setDescription(description);
        tx.setTimestamp(LocalDateTime.now());
        return tx;
    }
}