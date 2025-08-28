package com.BankApp.localbankapp.dto;

import com.BankApp.localbankapp.model.Currency;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Alexander Brazhkin
 */
@Data
@AllArgsConstructor
public class AccountDTO {

    @NotBlank(message = "User ID cannot be empty")
    private Long userId;

    @NotBlank(message = "Add non-empty currency")
    private Currency currency;
}
