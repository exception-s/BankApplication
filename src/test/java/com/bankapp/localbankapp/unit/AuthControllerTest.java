package com.bankapp.localbankapp.unit;

import com.BankApp.localbankapp.controller.AuthController;
import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.exception.GlobalExceptionHandler;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.service.AuthService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Alexander Brazhkin
 */
@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
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
