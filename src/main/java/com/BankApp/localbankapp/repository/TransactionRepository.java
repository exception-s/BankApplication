package com.BankApp.localbankapp.repository;

import com.BankApp.localbankapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Alexander Brazhkin
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountId(Long fromAccountId);
    List<Transaction> findByToAccountId(Long toAccountId);
}