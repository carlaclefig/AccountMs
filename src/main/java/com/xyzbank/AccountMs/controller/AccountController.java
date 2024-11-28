package com.xyzbank.AccountMs.controller;

import com.xyzbank.AccountMs.model.Account;
import com.xyzbank.AccountMs.service.AccountService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Object> createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PutMapping("/{accountId}/deposit")
    public ResponseEntity<Object> deposit(@PathVariable Long accountId, @RequestBody AmountRequest amountRequest) {
        return accountService.deposit(accountId, amountRequest.getAmount());
    }

    @PutMapping("/{accountId}/withdraw")
    public ResponseEntity<Object> withdraw(@PathVariable Long accountId, @RequestBody AmountRequest amountRequest) {
        return accountService.withdraw(accountId, amountRequest.getAmount());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAccount(@PathVariable Long id) {
        // Llamamos al servicio para eliminar la cuenta y devolver la respuesta adecuada
        return accountService.deleteAccount(id);
    }

    public static class AmountRequest {
        private Double amount;

        // Getters y Setters
        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }
}
