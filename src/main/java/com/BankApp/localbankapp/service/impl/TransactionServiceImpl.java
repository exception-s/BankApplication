package com.BankApp.localbankapp.service.impl;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.exception.AccountNotFoundException;
import com.BankApp.localbankapp.mapper.TransactionMapper;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.Currency;
import com.BankApp.localbankapp.model.Transaction;
import com.BankApp.localbankapp.model.TransactionType;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.TransactionRepository;
import com.BankApp.localbankapp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction transfer(TransactionDTO dto) {
        long fromId = dto.getFromAccountId();
        long toId = dto.getToAccountId();
        BigDecimal amount = dto.getAmount();
        Currency fromCurrency = dto.getFromCurrency();
        Currency toCurrency = dto.getToCurrency();
        BankAccount from = accountRepository.findById(fromId)
                                            .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
        BankAccount to = accountRepository.findById(toId)
                                          .orElseThrow(() -> new AccountNotFoundException("Target account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transaction tx = TransactionMapper.toEntity(
                new TransactionDTO(fromId, toId, amount, fromCurrency, toCurrency),
                from,
                to,
                TransactionType.TRANSFER,
                "Transfer"
        );

        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction deposit(TransactionDTO dto) {
        // todo (from 2025-08-29, 16:59): implement functionality
        long fromId = dto.getFromAccountId();
        long toId = dto.getToAccountId();
        BigDecimal amount = dto.getAmount();
        Currency fromCurrency = dto.getFromCurrency();
        Currency toCurrency = dto.getToCurrency();
        return null;
    }

    @Transactional
    public Transaction withdrawal(TransactionDTO dto) {
        // todo (from 2025-08-29, 16:59): implement functionality
        long fromId = dto.getFromAccountId();
        long toId = dto.getToAccountId();
        BigDecimal amount = dto.getAmount();
        Currency fromCurrency = dto.getFromCurrency();
        Currency toCurrency = dto.getToCurrency();
        return null;
    }
}