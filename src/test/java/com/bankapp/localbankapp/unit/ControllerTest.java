package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.controller.AccountController;
import com.BankApp.localbankapp.controller.AuthController;
import com.BankApp.localbankapp.controller.TransactionController;
import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.exception.GlobalExceptionHandler;
import com.BankApp.localbankapp.model.*;
import com.BankApp.localbankapp.service.AccountService;
import com.BankApp.localbankapp.service.AuthService;
import com.BankApp.localbankapp.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Alexander Brazhkin
 */
@Tag("Controllers")
@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
public class ControllerTest {
    @Nested
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
                    .andExpect(status().isOk())
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
                    .andExpect(status().isOk()).andReturn();

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

    @Nested
    @DisplayName("Auth controller testing")
    class AuthControllerTest {
        private MockMvc mockMvc;

        @Mock
        private AuthService authService;

        @InjectMocks
        private AuthController authController;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private AuthRequest authRequest;
        private User testUser;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

            testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");

            authRequest = new AuthRequest(
                    "testuser",
                    "password123",
                    "test@example.com"
            );
        }

        @Test
        void registerSuccess() throws Exception {
            when(authService.registerUser(any(AuthRequest.class))).thenReturn(testUser);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        @Test
        void registerInvalidData() throws Exception {
            AuthRequest invalidRequest = new AuthRequest(
                    "",
                    "",
                    ""
            );

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            Optional<UsernameNotFoundException> ex = Optional.ofNullable((UsernameNotFoundException) result.getResolvedException());
            ex.ifPresent(e -> assertEquals(e.getClass(), UsernameNotFoundException.class));
        }

        @Test
        void loginSuccess() throws Exception {
            when(authService.authenticateUser(any(AuthRequest.class))).thenReturn("jwt-token");

            mockMvc.perform(post("/api/auth/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("jwt-token"));
        }
    }

    @Nested
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
}
