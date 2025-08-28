package com.BankApp.localbankapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author Alexander Brazhkin
 */

@Data
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 5, max = 30)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 40)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;
}