package com.BankApp.localbankapp.controller;

import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Bank Accounts", description = "Управление банковскими счетами")
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Создать новый банковский счёт")
    @ApiResponse(responseCode = "200", description = "Счёт успешно создан")
    @PostMapping
    public ResponseEntity<BankAccount> createAccount(@RequestBody AccountDTO dto) {
        BankAccount account = accountService.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @Operation(summary = "Получить счёт по ID")
    @ApiResponse(responseCode = "200", description = "Счёт найден")
    @ApiResponse(responseCode = "404", description = "Счёт не найден")
    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable Long id) {
        BankAccount account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Получить баланс счета по ID")
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long accountId) {
        BigDecimal balance = accountService.getBalanceById(accountId);
        return ResponseEntity.ok(balance);
    }
}