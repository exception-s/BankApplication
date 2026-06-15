package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.Currency;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Brazhkin
 */
@DisplayName("Account service testing")
@Tag("AccountService")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

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
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.setContext(securityContext);

        BankAccount account = accountService.getAccountById(1L);
        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals("ACC_0001", account.getAccountNumber());
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAccountThrowsEmptyResultDataAccessException() {
        when(accountRepository.findById(500L)).thenReturn(Optional.empty());

        EmptyResultDataAccessException ex =
                assertThrows(EmptyResultDataAccessException.class, () -> accountService.getAccountById(500L));

        assertEquals("Account not found with id: " + 500L, ex.getMessage());
    }

    @Test
    @DisplayName("Should throw FORBIDDEN when user tries to access another user's account")
    void getAccountThrowsForbiddenForDifferentUser() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("hackerUser");
        SecurityContextHolder.setContext(securityContext);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> accountService.getAccountById(1L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        
        SecurityContextHolder.clearContext();
    }
}
