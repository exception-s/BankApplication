package com.BankApp.localbankapp.controller;

import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Alexander Brazhkin
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и логин пользователей")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody AuthRequest request) {
        User user = authService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Аутентификация и получение JWT токена")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        String token = authService.authenticateUser(request);
        return ResponseEntity.ok(token);
    }
}