package com.xyzbank.AccountMs.model;

import jakarta.persistence.*;
import java.util.Random;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private Double balance = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private Long customerId;

    public enum AccountType {
        SAVINGS, CHECKING
    }

    public Account() {}

    public Account(AccountType accountType, Long customerId) {
        this.accountNumber = generateAccountNumber();
        this.accountType = accountType;
        this.customerId = customerId;
        this.balance = 10.0; // Initial balance as specified
    }

    // Generate unique account number
    private String generateAccountNumber() {
        Random random = new Random();
        int number = 1000000000 + random.nextInt(900000000);
        return String.valueOf(number);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setBalance(Double balance) { this.balance = balance; }
}
