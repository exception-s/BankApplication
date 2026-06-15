package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.security.JwtAuthFilter;
import com.BankApp.localbankapp.security.JwtTokenProvider;
import com.BankApp.localbankapp.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * @author Alexander Brazhkin
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter testing")
class JwtAuthFilterTest {
    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalValidTokenSetsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT(token)).thenReturn("testuser");
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternalNoAuthorizationHeaderContinuesFilterChain() throws ServletException, IOException {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternalInvalidTokenContinuesFilterChain() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternalMalformedAuthorizationHeaderContinuesFilterChain() throws ServletException, IOException {
        request.addHeader("Authorization", "InvalidFormat");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternalUserNotFoundContinuesFilterChain() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT(token)).thenReturn("nonexistent");
        when(userService.loadUserByUsername("nonexistent"))
                .thenThrow(new RuntimeException("User not found"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void getJwtFromRequestValidAuthorizationHeaderReturnsToken() {
        request.addHeader("Authorization", "Bearer valid.jwt.token");

        String token = jwtAuthFilter.getJwtFromRequest(request);

        assertEquals("valid.jwt.token", token);
    }

    @Test
    void getJwtFromRequestNoBearerPrefixReturnsNull() {
        request.addHeader("Authorization", "Basic base64credentials");

        String token = jwtAuthFilter.getJwtFromRequest(request);

        assertNull(token);
    }

    @Test
    void getJwtFromRequestNoAuthorizationHeaderReturnsNull() {
        String token = jwtAuthFilter.getJwtFromRequest(request);

        assertNull(token);
    }
}
