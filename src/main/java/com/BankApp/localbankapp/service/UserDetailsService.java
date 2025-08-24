package com.BankApp.localbankapp.service;

import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * @author Alexander Brazhkin
 */
public interface UserDetailsService {
    User getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User updatedUser);
    UserDetails loadUserByUsername(String username);
}