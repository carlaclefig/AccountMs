package com.xyzbank.AccountMs.service.imp;

import com.xyzbank.AccountMs.model.Account;
import com.xyzbank.AccountMs.repository.AccountRepository;
import com.xyzbank.AccountMs.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    @Override
    public Account deposit(Long accountId, Double amount) {
        Account account = getAccountById(accountId);
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");

        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }

    @Override
    public Account withdraw(Long accountId, Double amount) {
        Account account = getAccountById(accountId);
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive");

        double newBalance = account.getBalance() - amount;
        if (account.getAccountType() == Account.AccountType.SAVINGS && newBalance < 0) {
            throw new IllegalArgumentException("No negative balance allowed in savings accounts.");
        }
        if (account.getAccountType() == Account.AccountType.CHECKING && newBalance < -500) {
            throw new IllegalArgumentException("Overdraft limit reached for checking accounts.");
        }
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    @Override
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
