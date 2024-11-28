package com.xyzbank.AccountMs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.Random;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public Account(AccountType accountType, Long customerId) {
        this.accountNumber = generateAccountNumber(); // Genera un número de cuenta único
        this.accountType = accountType;
        this.customerId = customerId;
        this.balance = 10.0; // Balance inicial
    }

    private String generateAccountNumber() {
        Random random = new Random();
        int number = 1000000000 + random.nextInt(900000000);
        return String.valueOf(number);
    }
}
