package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Brazhkin
 */
@ExtendWith(MockitoExtension.class)
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

        await().atMost(2, TimeUnit.MILLISECONDS);

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
