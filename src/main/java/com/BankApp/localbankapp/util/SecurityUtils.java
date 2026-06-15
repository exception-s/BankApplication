package com.BankApp.localbankapp.util;

import com.BankApp.localbankapp.model.BankAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utility class for security operations.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Verifies that the given bank account belongs to the currently authenticated user.
     * Throws a 403 Forbidden ResponseStatusException if it does not.
     *
     * @param account the bank account to check
     */
    public static void verifyAccountOwnership(BankAccount account) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!account.getUser().getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this account");
        }
    }
}
