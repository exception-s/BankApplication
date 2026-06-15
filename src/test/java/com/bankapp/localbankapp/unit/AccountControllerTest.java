package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.controller.AccountController;
import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.exception.GlobalExceptionHandler;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.Currency;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alexander Brazhkin
 */
@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Account controller testing")
class AccountControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BankAccount testAccount;
    private AccountDTO accountDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC_0001");
        testAccount.setBalance(BigDecimal.valueOf(1000));
        testAccount.setUser(testUser);
        testAccount.setCurrency(Currency.USD);

        accountDTO = new AccountDTO(1L, Currency.USD);
    }

    @Test
    void createAccountSuccess() throws Exception {
        when(accountService.createAccount(any(AccountDTO.class))).thenReturn(testAccount);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC_0001"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void createAccountInvalidData() throws Exception {
        AccountDTO invalidDTO = new AccountDTO(null, null);

        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isCreated()).andReturn();

        Optional<NullPointerException> ex = Optional.ofNullable((NullPointerException) result.getResolvedException());
        ex.ifPresent(e -> assertEquals(e.getClass(), NullPointerException.class));
    }

    @Test
    void getAccountSuccess() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(testAccount);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC_0001"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
