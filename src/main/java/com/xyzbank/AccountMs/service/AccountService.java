package com.xyzbank.AccountMs.service;

import com.xyzbank.AccountMs.model.Account;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AccountService {
    ResponseEntity<Object> createAccount(Account account);
    ResponseEntity<Object> getAccountById(Long id);
    List<Account> getAllAccounts();
    ResponseEntity<Object> deposit(Long accountId, Double amount);
    ResponseEntity<Object> withdraw(Long accountId, Double amount);
    ResponseEntity<Object> deleteAccount(Long id);
}

