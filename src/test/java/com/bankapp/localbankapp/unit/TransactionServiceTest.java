package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.dto.TransactionDTO;
import com.BankApp.localbankapp.model.*;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.TransactionRepository;
import com.BankApp.localbankapp.service.impl.TransactionServiceImpl;
import com.BankApp.localbankapp.util.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/**
 * @author Alexander Brazhkin
 */
@DisplayName("Transaction service testing")
@Tag("TransactionService")
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyConverter currencyConverter;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

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
        rubAccount.setBalance(BigDecimal.valueOf(5000));
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
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(toAccount));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);
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
        when(accountRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

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
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);
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
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(toAccount));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);
        TransactionDTO dto = new TransactionDTO(
                1L,
                2L,
                BigDecimal.valueOf(10000),
                Currency.USD,
                Currency.USD
        );
        RuntimeException ex = assertThrows(ResponseStatusException.class, () -> transactionService.transfer(dto));

        assertEquals("400 BAD_REQUEST \"Insufficient funds\"", ex.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferUSDToEURSuccess() {
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usdAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(eurAccount));
        when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.USD.toString(), Currency.EUR.toString()))
                .thenReturn(BigDecimal.valueOf(85)); // for example 1 USD = 0.85 EUR
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);
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

        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(eurAccount));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usdAccount));
        when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.EUR.toString(), Currency.USD.toString()))
                .thenReturn(BigDecimal.valueOf(118)); // for example 1 EUR = 1.18 USD
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User2");
        SecurityContextHolder.setContext(securityContext);
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

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usdAccount));
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(rubAccount));
        when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.USD.toString(), Currency.RUB.toString()))
                .thenReturn(BigDecimal.valueOf(7500)); // for example 1 USD = 75 RUB
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        Transaction result = transactionService.transfer(transactionDTO);

        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(0, BigDecimal.valueOf(900).compareTo(usdAccount.getBalance())); // 1000 - 100
        assertEquals(0, BigDecimal.valueOf(12500).compareTo(rubAccount.getBalance())); // 5000 + 7500
        assertEquals(Currency.USD, result.getFromCurrency());
        assertEquals(Currency.RUB, result.getToCurrency());
    }

    @Test
    void transferThrowsResponseStatusExceptionOnInsufficientFundsDifferentCurrency() {
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(rubAccount));
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(rubAccount));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User2");
        SecurityContextHolder.setContext(securityContext);
        TransactionDTO dto = new TransactionDTO(
                3L,
                3L,
                BigDecimal.valueOf(10000),
                Currency.RUB,
                Currency.RUB
        );
        RuntimeException ex = assertThrows(ResponseStatusException.class, () -> transactionService.transfer(dto));

        assertEquals("400 BAD_REQUEST \"Insufficient funds\"", ex.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void depositWithCurrencyConversionSuccess() {
        transactionDTO.setFromAccountId(null);
        transactionDTO.setFromCurrency(null);
        transactionDTO.setToAccountId(2L); // deposit in EUR account
        transactionDTO.setToCurrency(Currency.USD); // but in USD

        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(eurAccount));
        when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.USD.toString(), Currency.EUR.toString()))
                .thenReturn(BigDecimal.valueOf(85)); // USD to EUR
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User2");
        SecurityContextHolder.setContext(securityContext);
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

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usdAccount));
        when(currencyConverter.convert(BigDecimal.valueOf(100), Currency.EUR.toString(), Currency.USD.toString()))
                .thenReturn(BigDecimal.valueOf(118)); // EUR to USD
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);
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

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usdAccount));
        when(currencyConverter.convert(BigDecimal.valueOf(1000), Currency.EUR.toString(), Currency.USD.toString()))
                .thenReturn(BigDecimal.valueOf(1180)); // 1000 EUR to USD
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");
        SecurityContextHolder.setContext(securityContext);

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
