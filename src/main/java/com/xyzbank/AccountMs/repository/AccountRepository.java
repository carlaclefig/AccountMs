package com.xyzbank.AccountMs.repository;

import com.xyzbank.AccountMs.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);
}


