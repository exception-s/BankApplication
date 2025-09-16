package com.BankApp.localbankapp.controller;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.model.Transaction;
import com.BankApp.localbankapp.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Alexander Brazhkin
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Транзакции", description = "Операции перевода между счетами")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Выполнить перевод между счетами")
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestBody TransactionDTO dto) {
        Transaction transaction = transactionService.transfer(dto);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Пополнить счёт")
    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestBody TransactionDTO dto) {
        Transaction transaction = transactionService.deposit(dto);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Снять средства")
    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestBody TransactionDTO dto) {
        Transaction transaction = transactionService.withdrawal(dto);
        return ResponseEntity.ok(transaction);
    }
}
