package com.BankApp.localbankapp.dto;

import com.BankApp.localbankapp.model.Currency;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
@Data
@AllArgsConstructor
public class TransactionDTO {

    @NotBlank(message = "Id of the account from which the transfer is being made cannot be empty")
    private Long fromAccountId;

    @NotBlank(message = "Id of the account being transferred to cannot be empty")
    private Long toAccountId;

    @NotBlank(message = "An amount field cannot be empty(if amount is equal to zero, add 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency cannot be unfilled")
    private Currency fromCurrency;

    @NotBlank(message = "Currency cannot be unfilled")
    private Currency toCurrency;
}