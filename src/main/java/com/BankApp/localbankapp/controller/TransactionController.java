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
        Transaction transaction = transactionService.transfer(
                dto.getFromAccountId(),
                dto.getToAccountId(),
                dto.getAmount()
        );
        return ResponseEntity.ok(transaction);
    }
}
