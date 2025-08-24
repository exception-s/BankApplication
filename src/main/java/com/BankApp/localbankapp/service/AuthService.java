package com.BankApp.localbankapp.service;

import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.model.User;

/**
 * @author Alexander Brazhkin
 */
public interface AuthService {
    User registerUser(AuthRequest request);
    String authenticateUser(AuthRequest request);
}