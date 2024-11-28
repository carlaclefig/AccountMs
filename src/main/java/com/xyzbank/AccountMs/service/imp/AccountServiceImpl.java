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
        return ResponseEntity.status(200).body(createdAccount); // Devolvemos ResponseEntity con el estado 200 OK
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll(); // Este método no necesita ResponseEntity, ya que es una lista de cuentas
    }

    @Override
    public ResponseEntity<Object> getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return ResponseEntity.status(200).body(account); // Devolvemos ResponseEntity con la cuenta
    }

    @Override
    public ResponseEntity<Object> deposit(Long accountId, Double amount) {
        Account account = (Account) getAccountById(accountId).getBody(); // Obtenemos la cuenta desde el ResponseEntity
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return ResponseEntity.status(200).body(account); // Devolvemos ResponseEntity con el saldo actualizado
    }

    @Override
    public ResponseEntity<Object> withdraw(Long accountId, Double amount) {
        Account account = (Account) getAccountById(accountId).getBody();

        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        return ResponseEntity.status(200).body(account); // Devolvemos ResponseEntity con el saldo actualizado
    }

    @Override
    public ResponseEntity<Object> deleteAccount(Long id) {
        // Intentamos encontrar la cuenta por ID usando Optional
        Optional<Account> accountOptional = accountRepository.findById(id);

        // Si la cuenta está presente, la eliminamos
        if (accountOptional.isPresent()) {
            accountRepository.deleteById(id);
            return ResponseEntity.status(200).body("Account successfully deleted");
        }

        // Si no se encuentra la cuenta, devolvemos un mensaje con código 404
        return ResponseEntity.status(404).body("Account not found");
    }
}
