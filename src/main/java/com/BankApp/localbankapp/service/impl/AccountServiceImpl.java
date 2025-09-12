package com.BankApp.localbankapp.service.impl;

import com.BankApp.localbankapp.dto.AccountDTO;
import com.BankApp.localbankapp.mapper.AccountMapper;
import com.BankApp.localbankapp.model.BankAccount;
import com.BankApp.localbankapp.model.User;
import com.BankApp.localbankapp.repository.AccountRepository;
import com.BankApp.localbankapp.repository.UserRepository;
import com.BankApp.localbankapp.service.AccountService;
import com.BankApp.localbankapp.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Alexander Brazhkin
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public BankAccount createAccount(AccountDTO dto) {
        if (dto.getUserId() == null || dto.getCurrency() == null) {
            throw new NullPointerException();
        }
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EmptyResultDataAccessException("User not found with id: " + dto.getUserId(), 1));
        String accountNumber = generateAccountNumber();
        BankAccount account = AccountMapper.toEntity(dto, user, accountNumber);

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BankAccount getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException("Account not found with id: " + id, 1));
    }

    private String generateAccountNumber() {
        return AccountNumberGenerator.generate();
    }
}