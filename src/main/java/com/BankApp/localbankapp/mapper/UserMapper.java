package com.BankApp.localbankapp.mapper;

import com.BankApp.localbankapp.dto.AuthRequest;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author Alexander Brazhkin
 */
@Component
public class UserMapper {
    public static User toEntity(AuthRequest request, String encodedPassword) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRoles(Collections.singleton(UserRole.USER));
        return user;
    }
}
