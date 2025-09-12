package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.security.JwtAuthFilter;
import com.BankApp.localbankapp.security.JwtTokenProvider;
import com.BankApp.localbankapp.service.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Alexander Brazhkin
 */
@Tag("Security")
@ExtendWith(MockitoExtension.class)
public class JwtTest {

    @Nested
    @DisplayName("JwtAuthFilter testing")
    class JwtAuthFilterTest {
        @Mock
        private JwtTokenProvider tokenProvider;

        @Mock
        private UserDetailsService userService;

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

    @Nested
    @DisplayName("JwtTokenProvider testing")
    class JwtTokenProviderTest {
        private JwtTokenProvider jwtTokenProvider;
        private final String jwtSecret = "my-very-secret-key-12345-my-very-secret-key-12345";
        private final int jwtExpirationMs = 86400000;

        @BeforeEach
        void setUp() {
            jwtTokenProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
            ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);
        }

        @Test
        void generateTokenValidAuthenticationReturnsToken() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            String token = jwtTokenProvider.generateToken(authentication);

            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        void getUsernameFromJWTValidTokenReturnsUsername() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            String token = jwtTokenProvider.generateToken(authentication);
            String username = jwtTokenProvider.getUsernameFromJWT(token);

            assertEquals("testuser", username);
        }

        @Test
        void validateTokenValidTokenReturnsTrue() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            String token = jwtTokenProvider.generateToken(authentication);
            boolean isValid = jwtTokenProvider.validateToken(token);

            assertTrue(isValid);
        }

        @Test
        void validateTokenInvalidTokenReturnsFalse() {
            String invalidToken = "invalid.token.here";
            boolean isValid = jwtTokenProvider.validateToken(invalidToken);

            assertFalse(isValid);
        }

        @Test
        void validateTokenExpiredTokenReturnsFalse() {
            JwtTokenProvider providerWithShortExpiration = new JwtTokenProvider();
            ReflectionTestUtils.setField(providerWithShortExpiration, "jwtSecret", jwtSecret);
            ReflectionTestUtils.setField(providerWithShortExpiration, "jwtExpirationMs", 1); // 1ms expiration

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            String token = providerWithShortExpiration.generateToken(authentication);

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            boolean isValid = jwtTokenProvider.validateToken(token);

            assertFalse(isValid);
        }

        @Test
        void getSigningKeyReturnsValidKey() {
            SecretKey key = ReflectionTestUtils.invokeMethod(jwtTokenProvider, "getSigningKey");

            assertNotNull(key);
            assertEquals("HmacSHA384", key.getAlgorithm());
        }
    }
}
