package com.xyzbank.AccountMs.service.imp;

import com.xyzbank.AccountMs.model.Account;
import com.xyzbank.AccountMs.repository.AccountRepository;
import com.xyzbank.AccountMs.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public ResponseEntity<Object> createAccount(Account account) {
        Account createdAccount = accountRepository.save(account);
        return ResponseEntity.status(200).body(createdAccount);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public ResponseEntity<Object> getAccountById(Long id) {
        Optional<Account> accountOptional = accountRepository.findById(id);
        if (accountOptional.isPresent()) {
            return ResponseEntity.status(200).body(accountOptional.get());
        } else {
            return ResponseEntity.status(404).body("Account not found");
        }
    }

    @Override
    public ResponseEntity<Object> deposit(Long accountId, Double amount) {
        Account account = (Account) getAccountById(accountId).getBody();
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return ResponseEntity.status(200).body(account);
    }

    @Override
    public ResponseEntity<Object> withdraw(Long accountId, Double amount) {
        Account account = (Account) getAccountById(accountId).getBody();

        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (account.getAccountType() == Account.AccountType.SAVINGS && account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        if (account.getAccountType() == Account.AccountType.CHECKING && account.getBalance() - amount < -500) {
            throw new IllegalArgumentException("Insufficient balance (overdraft limit exceeded)");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        return ResponseEntity.ok(account);
    }

    @Override
    public ResponseEntity<Object> deleteAccount(Long id) {
        Optional<Account> accountOptional = accountRepository.findById(id);

        if (accountOptional.isPresent()) {
            accountRepository.deleteById(id);
            return ResponseEntity.status(200).body("Account successfully deleted");
        }

        return ResponseEntity.status(404).body("Account not found");
    }
}
