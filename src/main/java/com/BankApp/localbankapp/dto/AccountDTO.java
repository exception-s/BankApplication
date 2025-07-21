package com.BankApp.localbankapp.dto;

import com.BankApp.localbankapp.model.Currency;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Alexander Brazhkin
 */
@Data
public class AccountDTO {

    @NotBlank(message = "User ID cannot be empty")
    private Long userId;

    @NotBlank(message = "Add non-empty currency")
    private Currency currency;
}
