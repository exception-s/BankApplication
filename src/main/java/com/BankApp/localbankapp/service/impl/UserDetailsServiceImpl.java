package com.BankApp.localbankapp.service.impl;

import com.BankApp.localbankapp.exception.AccountNotFoundException;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.security.CustomUserDetails;
import com.BankApp.localbankapp.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexander Brazhkin
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                             .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setUsername(updatedUser.getUsername());
        existing.setPassword(updatedUser.getPassword());
        existing.setEmail(updatedUser.getEmail());
        // Пароль должен обновляться отдельно с хешированием
        return userRepository.save(existing);
    }

    public CustomUserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                                  .orElseThrow(() -> new AccountNotFoundException(username));

        List<GrantedAuthority> authorities = user.getRoles()
                                                 .stream()
                                                 .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                                 .collect(Collectors.toList());

        return new CustomUserDetails(user, authorities);
    }
}