package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.controller.TransactionController;
import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.exception.GlobalExceptionHandler;
import com.BankApp.localbankapp.model.*;
import com.BankApp.localbankapp.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alexander Brazhkin
 */
@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Transaction controller testing")
class TransactionControllerTest {
    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private TransactionDTO transactionDTO;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();

        User fromUser = new User();
        fromUser.setId(1L);
        fromUser.setUsername("user1");

        User toUser = new User();
        toUser.setId(2L);
        toUser.setUsername("user2");

        BankAccount fromAccount = new BankAccount();
        fromAccount.setId(1L);
        fromAccount.setAccountNumber("ACC_0001");
        fromAccount.setUser(fromUser);
        fromAccount.setCurrency(Currency.USD);

        BankAccount toAccount = new BankAccount();
        toAccount.setId(2L);
        toAccount.setAccountNumber("ACC-2001");
        toAccount.setUser(toUser);
        toAccount.setCurrency(Currency.USD);

        transactionDTO = new TransactionDTO(
                1L,
                999L,
                BigDecimal.valueOf(100),
                Currency.USD,
                Currency.USD
        );

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setFromAccount(fromAccount);
        testTransaction.setToAccount(toAccount);
        testTransaction.setAmount(BigDecimal.valueOf(100));
        testTransaction.setType(TransactionType.TRANSFER);
        testTransaction.setTimestamp(LocalDateTime.now());
    }

    @Test
    void transferSuccess() throws Exception {
        when(transactionService.transfer(any(TransactionDTO.class))).thenReturn(testTransaction);

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("TRANSFER"));
    }

    @Test
    void transferInsufficientFunds() throws Exception {
        when(transactionService.transfer(any(TransactionDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());
    }
}
