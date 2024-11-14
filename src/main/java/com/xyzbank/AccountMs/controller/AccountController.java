package com.xyzbank.AccountMs.controller;

import com.xyzbank.AccountMs.model.Account;
import com.xyzbank.AccountMs.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public Account getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    public static class AmountRequest {
        private Double amount;

        // Getters y setters
        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }

    @PutMapping("/{accountId}/deposit")
    public Account deposit(@PathVariable Long accountId, @RequestBody AmountRequest amountRequest) {
        return accountService.deposit(accountId, amountRequest.getAmount());
    }

    @PutMapping("/{accountId}/withdraw")
    public Account withdraw(@PathVariable Long accountId, @RequestBody AmountRequest amountRequest) {
        return accountService.withdraw(accountId, amountRequest.getAmount());
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
