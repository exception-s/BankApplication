package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.exception.EmailNotFoundException;
import com.BankApp.localbankapp.model.*;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.TransactionRepository;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.security.JwtTokenProvider;
import com.BankApp.localbankapp.service.impl.AccountServiceImpl;
import com.BankApp.localbankapp.service.impl.AuthServiceImpl;
import com.BankApp.localbankapp.service.impl.TransactionServiceImpl;
import com.BankApp.localbankapp.service.impl.UserServiceImpl;
import com.BankApp.localbankapp.util.CurrencyConverter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Alexander Brazhkin
 */
@Tag("Services")
@ExtendWith(MockitoExtension.class)
public class ServiceTest {

    @Nested
    @DisplayName("Account service testing")
    class AccountServiceTest {

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private AccountServiceImpl accountService;

        private User testUser;
        private BankAccount testAccount;

        @BeforeEach
        void setUp() {
            testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testUser");
            testUser.setPassword("testPass");

            testAccount = new BankAccount();
            testAccount.setId(1L);
            testAccount.setAccountNumber("ACC_0001");
            testAccount.setUser(testUser);
            testAccount.setBalance(BigDecimal.valueOf(1000));
            testAccount.setCurrency(Currency.USD);
        }

        @Test
        void createAccountSuccess() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
                BankAccount account = invocation.getArgument(0);
                account.setId(1L);
                account.setAccountNumber("ACC-1001");
                return account;
            });

            AccountDTO accDTO = new AccountDTO(1L, Currency.USD);
            BankAccount account = accountService.createAccount(accDTO);

            assertNotNull(account);
            assertEquals(1L, account.getId());
            assertEquals(Currency.USD, account.getCurrency());
            assertNotNull(account.getAccountNumber());
            assertEquals(testUser, account.getUser());
            verify(accountRepository, times(1)).save(any(BankAccount.class));
        }

        @Test
        void createAccountThrowsEmptyResultDataAccessException() {
            when(userRepository.findById(500L)).thenReturn(Optional.empty());
            AccountDTO accDTO = new AccountDTO(500L, Currency.USD);
            assertThrows(EmptyResultDataAccessException.class, () -> accountService.createAccount(accDTO));

            verify(accountRepository, never()).save(any(BankAccount.class));
        }

        @Test
        void getAccountSuccess() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

            BankAccount account = accountService.getAccountById(1L);
            assertNotNull(account);
            assertEquals(1L, account.getId());
            assertEquals("ACC_0001", account.getAccountNumber());
        }

        @Test
        void getAccountThrowsEmptyResultDataAccessException() {
            when(accountRepository.findById(500L)).thenReturn(Optional.empty());

            EmptyResultDataAccessException ex =
                    assertThrows(EmptyResultDataAccessException.class, () -> accountService.getAccountById(500L));

            assertEquals("Account not found with id: " + 500L, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Auth service testing")
    @Tag("AuthService")
    class AuthServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private JwtTokenProvider tokenProvider;

        @InjectMocks
        private AuthServiceImpl authService;

        private User testUser;
        private AuthRequest request;

        @BeforeEach
        void setUp() {
            testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testUser");
            testUser.setPassword("testPass");
            testUser.setEmail("testUser@test.com");

            request = new AuthRequest(
                    testUser.getUsername(),
                    testUser.getPassword(),
                    testUser.getEmail()
            );
        }

        @Test
        void registerUserSuccess() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("testUser@test.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("testPass")).thenReturn("hashedPass");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                user.setUsername("testUser");
                return user;
            });

            User registeredUser = authService.registerUser(request);

            assertNotNull(registeredUser);
            assertEquals(1L, registeredUser.getId());
            assertEquals("testUser", registeredUser.getUsername());
            assertEquals("hashedPass", registeredUser.getPassword());
            assertEquals("testUser@test.com", registeredUser.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        void registerUserThrowsUsernameNotFoundExceptionOnUsername() {
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

            RuntimeException ex = assertThrows(UsernameNotFoundException.class, () -> authService.registerUser(request));
            assertEquals("Username is already taken", ex.getMessage());
            verify(userRepository, times(1)).findByUsername("testUser");
            verify(userRepository, never()).findByEmail(anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void registerUserThrowsEmailNotFoundExceptionOnEmail() {
            when(userRepository.findByEmail("testUser@test.com")).thenReturn(Optional.of(testUser));

            assertThrows(EmailNotFoundException.class, () -> authService.registerUser(request));
            verify(userRepository, times(1)).findByEmail("testUser@test.com");
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void authenticateUserSuccess() {
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                      .thenReturn(authentication);
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

            String token = authService.authenticateUser(request);

            assertNotNull(token);
            assertEquals("jwt-token", token);
            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenProvider, times(1)).generateToken(authentication);
        }

        @Test
        void authenticateUserInvalidCredentials() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                      .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.authenticateUser(request));

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenProvider, never()).generateToken(any());
        }

        @Test
        void authenticateUserAuthenticationManagerThrowsException() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                      .thenThrow(new AuthenticationException("Authentication service unavailable") {});

            assertThrows(AuthenticationException.class, () -> authService.authenticateUser(request));

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenProvider, never()).generateToken(any());
        }
    }

    @Nested
    @DisplayName("Transaction service testing")
    @Tag("TransactionService")
    class TransactionServiceTest {

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private CurrencyConverter currencyConverter;

        @Mock
        private TransactionRepository transactionRepository;

        @InjectMocks
        private TransactionServiceImpl transactionService;
        private BankAccount fromAccount;
        private BankAccount toAccount;
        private BankAccount usdAccount;
        private BankAccount eurAccount;
        private BankAccount rubAccount;
        private TransactionDTO transactionDTO;


        @BeforeEach
        void setUp() {
            User user1 = new User();
            user1.setId(1L);
            user1.setUsername("User1");
            user1.setPassword("pass1");
            user1.setEmail("User1@test.com");

            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("User2");
            user2.setPassword("pass2");
            user2.setEmail("User2@test.com");

            fromAccount = new BankAccount();
            fromAccount.setId(1L);
            fromAccount.setAccountNumber("ACC_0001");
            fromAccount.setBalance(BigDecimal.valueOf(1000));
            fromAccount.setUser(user1);
            fromAccount.setCurrency(Currency.USD);
            fromAccount.setActive(true);

            toAccount = new BankAccount();
            toAccount.setId(2L);
            toAccount.setAccountNumber("ACC_0002");
            toAccount.setBalance(BigDecimal.valueOf(500));
            toAccount.setUser(user2);
            toAccount.setCurrency(Currency.USD);
            toAccount.setActive(true);

            usdAccount = new BankAccount();
            usdAccount.setId(1L);
            usdAccount.setAccountNumber("ACC_USD_001");
            usdAccount.setBalance(BigDecimal.valueOf(1000));
            usdAccount.setUser(user1);
            usdAccount.setCurrency(Currency.USD);

            eurAccount = new BankAccount();
            eurAccount.setId(2L);
            eurAccount.setAccountNumber("ACC_EUR_001");
            eurAccount.setBalance(BigDecimal.valueOf(500));
            eurAccount.setUser(user2);
            eurAccount.setCurrency(Currency.EUR);

            rubAccount = new BankAccount();
            rubAccount.setId(3L);
            rubAccount.setAccountNumber("ACC_RUB_001");
            rubAccount.setBalance(BigDecimal.valueOf(50000));
            rubAccount.setUser(user2);
            rubAccount.setCurrency(Currency.RUB);

            transactionDTO = new TransactionDTO(
                    1L,
                    2L,
                    BigDecimal.valueOf(100),
                    Currency.USD,
                    Currency.EUR
            );
        }

        @Test
        void transferSuccessSameCurrency() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setId(1L);
                return transaction;
            });

            TransactionDTO dto = new TransactionDTO(
                    1L,
                    2L,
                    BigDecimal.valueOf(300),
                    Currency.USD,
                    Currency.USD
            );
            Transaction transaction = transactionService.transfer(dto);

            assertNotNull(transaction);
            assertEquals(1L, transaction.getFromAccount().getId());
            assertEquals(2L, transaction.getToAccount().getId());
            assertEquals(TransactionType.TRANSFER, transaction.getType());
            assertEquals(0, BigDecimal.valueOf(700).compareTo(fromAccount.getBalance()));
            assertEquals(0, BigDecimal.valueOf(800).compareTo(toAccount.getBalance()));
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        void transferThrowsResponseStatusExceptionOnFromId() {
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            TransactionDTO dto = new TransactionDTO(
                    999L,
                    2L,
                    BigDecimal.valueOf(100),
                    Currency.USD,
                    Currency.USD
            );

            var ex = assertThrows(ResponseStatusException.class, () -> transactionService.transfer(dto));
            assertEquals("404 NOT_FOUND \"Source account not found\"", ex.getMessage());
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        void transferThrowsResponseStatusExceptionOnToId() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());
            TransactionDTO dto = new TransactionDTO(
                    1L,
                    999L,
                    BigDecimal.valueOf(100),
                    Currency.USD,
                    Currency.USD
            );
            var ex = assertThrows(ResponseStatusException.class, () -> {
                transactionService.transfer(dto);
            });
            assertEquals("404 NOT_FOUND \"Target account not found\"", ex.getMessage());
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        void transferThrowsResponseStatusExceptionOnInsufficientFunds() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

            TransactionDTO dto = new TransactionDTO(
                    1L,
                    2L,
                    BigDecimal.valueOf(10000),
                    Currency.USD,
                    Currency.USD
            );
            RuntimeException ex = assertThrows(ResponseStatusException.class, () -> transactionService.transfer(dto));

            assertEquals(ex.getMessage(), "400 BAD_REQUEST \"Insufficient funds\"");
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        void transferUSDToEURSuccess() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(usdAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(eurAccount));
            when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.USD.toString(), Currency.EUR.toString()))
                    .thenReturn(BigDecimal.valueOf(85)); // for example 1 USD = 0.85 EUR
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setId(1L);
                return transaction;
            });

            Transaction result = transactionService.transfer(transactionDTO);
            assertNotNull(result);
            assertEquals(TransactionType.TRANSFER, result.getType());
            assertEquals(0, BigDecimal.valueOf(900).compareTo(usdAccount.getBalance())); // 1000 - 100
            assertEquals(0, BigDecimal.valueOf(585).compareTo(eurAccount.getBalance())); // 500 + 85
            assertEquals(Currency.USD, result.getFromCurrency());
            assertEquals(Currency.EUR, result.getToCurrency());
            verify(currencyConverter, times(1)).convert(any(), any(), any());
        }

        @Test
        void transferEURToUSDSuccess() {
            transactionDTO.setFromAccountId(2L);
            transactionDTO.setToAccountId(1L);
            transactionDTO.setFromCurrency(Currency.EUR);
            transactionDTO.setToCurrency(Currency.USD);
            transactionDTO.setAmount(BigDecimal.valueOf(100));

            when(accountRepository.findById(2L)).thenReturn(Optional.of(eurAccount));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(usdAccount));
            when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.EUR.toString(), Currency.USD.toString()))
                    .thenReturn(BigDecimal.valueOf(118)); // for example 1 EUR = 1.18 USD
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setId(1L);
                return transaction;
            });

            Transaction result = transactionService.transfer(transactionDTO);

            assertNotNull(result);
            assertEquals(TransactionType.TRANSFER, result.getType());
            assertEquals(0, BigDecimal.valueOf(400).compareTo(eurAccount.getBalance())); // 500 - 100
            assertEquals(0, BigDecimal.valueOf(1118).compareTo(usdAccount.getBalance())); // 1000 + 118
            assertEquals(Currency.EUR, result.getFromCurrency());
            assertEquals(Currency.USD, result.getToCurrency());
        }

        @Test
        void transferUSDToRUBSuccess() {
            transactionDTO.setToAccountId(3L);
            transactionDTO.setToCurrency(Currency.RUB);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(usdAccount));
            when(accountRepository.findById(3L)).thenReturn(Optional.of(rubAccount));
            when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.USD.toString(), Currency.RUB.toString()))
                    .thenReturn(BigDecimal.valueOf(7500)); // for example 1 USD = 75 RUB
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setId(1L);
                return transaction;
            });

            Transaction result = transactionService.transfer(transactionDTO);

            assertNotNull(result);
            assertEquals(TransactionType.TRANSFER, result.getType());
            assertEquals(0, BigDecimal.valueOf(900).compareTo(usdAccount.getBalance())); // 1000 - 100
            assertEquals(0, BigDecimal.valueOf(57500).compareTo(rubAccount.getBalance())); // 50000 + 7500
            assertEquals(Currency.USD, result.getFromCurrency());
            assertEquals(Currency.RUB, result.getToCurrency());
        }

        @Test
        void transferThrowsResponseStatusExceptionOnInsufficientFundsDifferentCurrency() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

            TransactionDTO dto = new TransactionDTO(
                    1L,
                    2L,
                    BigDecimal.valueOf(10000),
                    Currency.USD,
                    Currency.USD
            );
            RuntimeException ex = assertThrows(ResponseStatusException.class, () -> transactionService.transfer(dto));

            assertEquals(ex.getMessage(), "400 BAD_REQUEST \"Insufficient funds\"");
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        void depositWithCurrencyConversionSuccess() {
            transactionDTO.setFromAccountId(null);
            transactionDTO.setFromCurrency(null);
            transactionDTO.setToAccountId(2L); // deposit in EUR account
            transactionDTO.setToCurrency(Currency.USD); // but in USD

            when(accountRepository.findById(2L)).thenReturn(Optional.of(eurAccount));
            when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.USD.toString(), Currency.EUR.toString()))
                    .thenReturn(BigDecimal.valueOf(85)); // USD to EUR
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setId(1L);
                return transaction;
            });

            Transaction result = transactionService.deposit(transactionDTO);

            assertNotNull(result);
            assertEquals(TransactionType.DEPOSIT, result.getType());
            assertEquals(0, BigDecimal.valueOf(585).compareTo(eurAccount.getBalance())); // 500 + 85
            assertNull(result.getFromAccount());
            assertEquals(eurAccount, result.getToAccount());
            assertEquals(Currency.USD, result.getToCurrency());
        }

        @Test
        void withdrawWithCurrencyConversionSuccess() {
            transactionDTO.setToAccountId(null);
            transactionDTO.setToCurrency(null);
            transactionDTO.setFromAccountId(1L); // from USD account
            transactionDTO.setFromCurrency(Currency.EUR); // but in EUR

            when(accountRepository.findById(1L)).thenReturn(Optional.of(usdAccount));
            when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.EUR.toString(), Currency.USD.toString()))
                    .thenReturn(BigDecimal.valueOf(118)); // EUR to USD
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setId(1L);
                return transaction;
            });

            Transaction result = transactionService.withdrawal(transactionDTO);

            assertNotNull(result);
            assertEquals(TransactionType.WITHDRAWAL, result.getType());
            assertEquals(0, BigDecimal.valueOf(882).compareTo(usdAccount.getBalance())); // 1000 - 118
            assertNull(result.getToAccount());
            assertEquals(usdAccount, result.getFromAccount());
            assertEquals(Currency.EUR, result.getFromCurrency());
        }

        @Test
        void withdrawInsufficientFundsThrowsException() {
            transactionDTO.setToAccountId(null);
            transactionDTO.setToCurrency(null);
            transactionDTO.setFromAccountId(1L);
            transactionDTO.setFromCurrency(Currency.EUR);
            transactionDTO.setAmount(BigDecimal.valueOf(1000));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(usdAccount));
            when(currencyConverter.convert(BigDecimal.valueOf(1000), Currency.EUR.toString(), Currency.USD.toString()))
                    .thenReturn(BigDecimal.valueOf(1180)); // 1000 EUR to USD

            assertThrows(ResponseStatusException.class, () -> {
                transactionService.withdrawal(transactionDTO);
            });
            assertEquals(0, BigDecimal.valueOf(1000).compareTo(usdAccount.getBalance()));
            verify(transactionRepository, never()).save(any());
        }

        @Test
        void transferNegativeAmountThrowsException() {
            transactionDTO.setAmount(BigDecimal.valueOf(-100));

            assertThrows(ResponseStatusException.class, () -> {
                transactionService.transfer(transactionDTO);
            });
            verify(accountRepository, never()).findById(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("User service testing")
    @Tag("UserService")
    class UserServiceTest {
        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private UserServiceImpl userService;
        private User testUser;

        @BeforeEach
        void setUp() {
            testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testUser");
            testUser.setPassword("testPass");
            testUser.setEmail("testUser@test.com");
        }

        @Test
        void getUserByIdSuccess() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            User user = userService.getUserById(1L);
            assertNotNull(user);
            assertEquals(1L, user.getId());
            assertEquals("testUser", user.getUsername());
            assertEquals("testPass", user.getPassword());
        }

        @Test
        void getUserByIdThrowsAEmptyResultDataAccessException() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EmptyResultDataAccessException.class, () -> userService.getUserById(1L));
        }

        @Test
        void getAllUsersSuccess() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<User> users = userService.getAllUsers();
            assertNotNull(users);
            assertEquals(1, users.size());
            assertEquals(1L, users.getFirst().getId());
            assertEquals("testUser", users.getFirst().getUsername());
        }

        @Test
        void getAllUsersReturnsEmptyList() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            List<User> users = userService.getAllUsers();
            assertNotNull(users);
            assertEquals(0, users.size());
        }

        @Test
        void updateUserSuccess() {
            User newUser = new User();
            newUser.setId(10L);
            newUser.setUsername("newUser");
            newUser.setPassword("newPass");
            newUser.setEmail("newUser@test.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User updated = userService.updateUser(1L, newUser);
            assertNotNull(updated);
            assertEquals(1L, updated.getId());
            assertEquals("newUser", updated.getUsername());
            assertEquals("newPass", updated.getPassword());
            assertEquals("newUser@test.com", updated.getEmail());
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        void updateNonExistingUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(EmptyResultDataAccessException.class, () -> userService.updateUser(1L, testUser));
        }

        @Test
        void loadUserByUsernameSuccess() {
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

            UserDetails user = userService.loadUserByUsername(testUser.getUsername());
            assertNotNull(user);
            assertEquals(testUser.getUsername(), user.getUsername());
            assertEquals(testUser.getPassword(), user.getPassword());
            assertLinesMatch(user.getAuthorities().stream().map(String::valueOf).toList(), List.of("ROLE_USER"));
        }

        @Test
        void loadUserByUsernameThrowsAccountNotFoundException() {
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(testUser.getUsername()));
        }
    }
}
