package com.BankApp.localbankapp.service.impl;

import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.mapper.UserMapper;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.model.UserRole;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.security.JwtTokenProvider;
import com.BankApp.localbankapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Alexander Brazhkin
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public User registerUser(AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already in use");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, encodedPassword);

        return userRepository.save(user);
    }

    public String authenticateUser(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }
}