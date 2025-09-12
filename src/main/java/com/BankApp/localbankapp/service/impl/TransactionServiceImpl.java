package com.BankApp.localbankapp.service.impl;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.mapper.TransactionMapper;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.Currency;
import com.BankApp.localbankapp.model.Transaction;
import com.BankApp.localbankapp.model.TransactionType;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.TransactionRepository;
import com.BankApp.localbankapp.service.TransactionService;
import com.BankApp.localbankapp.util.CurrencyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyConverter currencyConverter;

    @Transactional
    public Transaction transfer(TransactionDTO dto) {
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        long fromId = dto.getFromAccountId();
        long toId = dto.getToAccountId();
        BankAccount fromAccount = accountRepository.findById(fromId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));
        BankAccount toAccount = accountRepository.findById(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found"));

        Currency fromCurrency = dto.getFromCurrency() != null ? dto.getFromCurrency() : fromAccount.getCurrency();
        Currency toCurrency = dto.getToCurrency() != null ? dto.getToCurrency() : toAccount.getCurrency();

        BigDecimal amountToTransfer = dto.getAmount();
        if (!fromCurrency.equals(fromAccount.getCurrency())) {
            amountToTransfer = currencyConverter.convert(
                    amountToTransfer, fromCurrency.toString(), fromAccount.getCurrency().toString()
            );
        }

        if (fromAccount.getBalance().compareTo(amountToTransfer) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal amountToReceive = dto.getAmount();
        if (!fromCurrency.equals(toCurrency)) {
            amountToReceive = currencyConverter.convert(
                    amountToReceive, fromCurrency.toString(), toCurrency.toString()
            );
        }
        if (!toCurrency.equals(toAccount.getCurrency())) {
            amountToReceive = currencyConverter.convert(
                    amountToReceive, toCurrency.toString(), toAccount.getCurrency().toString()
            );
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amountToTransfer));
        toAccount.setBalance(toAccount.getBalance().add(amountToReceive));

        Transaction tx = TransactionMapper.toEntity(
                dto,
                fromAccount,
                toAccount,
                TransactionType.TRANSFER,
                "Transfer"
        );
        tx.setFromCurrency(fromCurrency);
        tx.setToCurrency(toCurrency);

        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction deposit(TransactionDTO dto) {
        long toId = dto.getToAccountId();
        BigDecimal amount = dto.getAmount();
        BankAccount depositAccount = accountRepository.findById(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deposit account is not found"));
        Currency toCurrency = dto.getToCurrency() != null ? dto.getToCurrency() : depositAccount.getCurrency();

        if (!toCurrency.equals(depositAccount.getCurrency())) {
            amount = currencyConverter.convert(amount, toCurrency.toString(), depositAccount.getCurrency().toString());
        }

        depositAccount.setBalance(depositAccount.getBalance().add(amount));
        Transaction tx = TransactionMapper.toEntity(
                dto,
                null,
                depositAccount,
                TransactionType.DEPOSIT,
                "Deposit"
        );
        tx.setFromCurrency(null);
        tx.setToCurrency(toCurrency);

        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction withdrawal(TransactionDTO dto) {
        long fromId = dto.getFromAccountId();
        BigDecimal amount = dto.getAmount();
        BankAccount withdrawalAccount = accountRepository.findById(fromId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Withdrawal account is not found"));

        Currency fromCurrency = dto.getFromCurrency() != null ? dto.getFromCurrency() : withdrawalAccount.getCurrency();
        if (!fromCurrency.equals(withdrawalAccount.getCurrency())) {
            amount = currencyConverter.convert(amount, fromCurrency.toString(), withdrawalAccount.getCurrency().toString());
        }
        if (withdrawalAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        withdrawalAccount.setBalance(withdrawalAccount.getBalance().subtract(amount));

        Transaction tx = TransactionMapper.toEntity(
                dto,
                withdrawalAccount,
                null,
                TransactionType.WITHDRAWAL,
                "Withdrawal"
        );
        tx.setFromCurrency(fromCurrency);
        tx.setToCurrency(null);

        return transactionRepository.save(tx);
    }
}