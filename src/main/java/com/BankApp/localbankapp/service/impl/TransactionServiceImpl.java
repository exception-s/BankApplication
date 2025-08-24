package com.BankApp.localbankapp.service.impl;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.mapper.TransactionMapper;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.Transaction;
import com.BankApp.localbankapp.model.TransactionType;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.TransactionRepository;
import com.BankApp.localbankapp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Alexander Brazhkin
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    @Transactional
    public Transaction transfer(Long fromId, Long toId, BigDecimal amount) {
        BankAccount from = accountRepo.findById(fromId)
                                      .orElseThrow(() -> new RuntimeException("Source account not found"));
        BankAccount to = accountRepo.findById(toId)
                                    .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transaction tx = TransactionMapper.toEntity(
                new TransactionDTO(fromId, toId, amount),
                from,
                to,
                TransactionType.TRANSFER,
                "Transfer"
        );

        return transactionRepo.save(tx);
    }
}