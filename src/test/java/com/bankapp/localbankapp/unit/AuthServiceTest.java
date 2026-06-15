package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.exception.EmailNotFoundException;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.security.JwtTokenProvider;
import com.BankApp.localbankapp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/**
 * @author Alexander Brazhkin
 */
@DisplayName("Auth service testing")
@Tag("AuthService")
@ExtendWith(MockitoExtension.class)
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
