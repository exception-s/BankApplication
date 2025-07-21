package com.BankApp.localbankapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

/**
 * @author Alexander Brazhkin
 */
@Data
public class TransactionDTO {

    @NotBlank(message = "Id of the account from which the transfer is being made cannot be empty")
    private Long fromAccountId;

    @NotBlank(message = "Id of the account being transferred to cannot be empty")
    private Long toAccountId;

    @NotBlank(message = "An amount field cannot be empty(if amount is equal to zero, add 0")
    private BigDecimal amount;
}