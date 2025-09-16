package com.BankApp.localbankapp.service;

import com.BankApp.localbankapp.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * @author Alexander Brazhkin
 */
public interface UserService extends UserDetailsService {
    User getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User updatedUser);
    UserDetails loadUserByUsername(String username);
}