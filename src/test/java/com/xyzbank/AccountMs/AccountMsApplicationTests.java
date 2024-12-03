package com.xyzbank.AccountMs;

import com.xyzbank.AccountMs.controller.AccountController;
import com.xyzbank.AccountMs.model.Account;
import com.xyzbank.AccountMs.repository.AccountRepository;
import com.xyzbank.AccountMs.service.AccountService;
import com.xyzbank.AccountMs.service.imp.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig(Account.class)
class AccountMsApplicationTests {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private AccountService accountService;

	@InjectMocks
	private AccountServiceImpl accountServiceImpl;

	@InjectMocks
	private AccountController accountController;

	private Account[] accountsArray;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		accountsArray = createAccounts();

		mockAccountRepository();
		mockAccountService();

		accountController = new AccountController(accountService);
	}

	private Account[] createAccounts() {
		Account[] accounts = new Account[] {
				new Account(Account.AccountType.SAVINGS, 1L),
				new Account(Account.AccountType.CHECKING, 2L),
				new Account(Account.AccountType.SAVINGS, 3L)
		};
		accounts[0].setBalance(1000.0);
		accounts[1].setBalance(1500.0);
		accounts[2].setBalance(2000.0);
		return accounts;
	}

	private void mockAccountRepository() {
		when(accountRepository.findById(1L)).thenReturn(Optional.of(accountsArray[0]));
		when(accountRepository.findById(2L)).thenReturn(Optional.of(accountsArray[1]));
		when(accountRepository.findById(3L)).thenReturn(Optional.of(accountsArray[2]));
		when(accountRepository.save(any(Account.class))).thenReturn(accountsArray[0]);
		when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
		when(accountRepository.findAll()).thenReturn(Arrays.asList(accountsArray));
	}

	private void mockAccountService() {
		when(accountService.getAllAccounts()).thenReturn(Arrays.asList(accountsArray[0], accountsArray[1], accountsArray[2]));
	}

	private void assertAccountBalance(Account account, double expectedBalance) {
		assertNotNull(account);
		assertEquals(expectedBalance, account.getBalance(), 500.0);
	}

	@Test
	public void testCreateAccount() {
		Account accountToCreate = new Account(Account.AccountType.CHECKING, 1L);
		accountToCreate.setBalance(1000.0);

		when(accountRepository.save(any(Account.class))).thenReturn(accountToCreate);

		ResponseEntity<Object> response = accountServiceImpl.createAccount(accountToCreate);

		assertEquals(200, response.getStatusCodeValue());
		Account result = (Account) response.getBody();
		assertNotNull(result);
		assertEquals(1000.0, result.getBalance());
	}

	@Test
	public void testGetAllAccounts_Service() {
		when(accountRepository.findAll()).thenReturn(Arrays.asList(accountsArray[0], accountsArray[1], accountsArray[2]));

		List<Account> response = accountServiceImpl.getAllAccounts();

		assertNotNull(response);
		assertEquals(3, response.size());

		assertEquals(accountsArray[0], response.get(0));
		assertEquals(accountsArray[1], response.get(1));
		assertEquals(accountsArray[2], response.get(2));

		verify(accountRepository, times(1)).findAll();
	}


	@Test
	public void testGetAccountById() {
		ResponseEntity<Object> response = accountServiceImpl.getAccountById(1L);

		assertEquals(200, response.getStatusCodeValue());
		Account result = (Account) response.getBody();
		assertNotNull(result);
		assertNotNull(result.getAccountNumber());
		assertEquals(10, result.getAccountNumber().length());
	}

	@Test
	public void testGetAccountById_AccountNotFound() {
		when(accountRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseEntity<Object> response = accountServiceImpl.getAccountById(99L);

		assertEquals(404, response.getStatusCodeValue());

		String message = (String) response.getBody();
		assertNotNull(message);
		assertEquals("Account not found", message);
	}

	@Test
	public void testWithdraw_InsufficientBalance_Savings() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			accountServiceImpl.withdraw(1L, 1500.0);  // Monto mayor que el saldo disponible
		});
		assertEquals("Insufficient balance", thrown.getMessage());
	}

	@Test
	public void testWithdraw_InsufficientBalance_Checking_OverdraftExceeded() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			accountServiceImpl.withdraw(2L, 2100.0);  // Monto mayor que el saldo + el límite de sobregiro (-500)
		});
		assertEquals("Insufficient balance (overdraft limit exceeded)", thrown.getMessage());
	}

	@Test
	public void testWithdraw_SuccessfulWithdrawal_Checking() {
		double withdrawalAmount = 1800.0;

		when(accountRepository.findById(2L)).thenReturn(Optional.of(accountsArray[1]));
		when(accountRepository.save(any(Account.class))).thenReturn(accountsArray[1]);

		ResponseEntity<Object> response = accountServiceImpl.withdraw(2L, withdrawalAmount);

		assertEquals(200, response.getStatusCodeValue());

		Account updatedAccount = (Account) response.getBody();
		assertEquals(1500.0 - 1800.0, updatedAccount.getBalance(), 0.0);  // Saldo actualizado: -300

		verify(accountRepository, times(1)).findById(2L);
		verify(accountRepository, times(1)).save(any(Account.class));
	}

	@Test
	public void testWithdraw_SuccessfulWithdrawal_Savings() {
		double withdrawalAmount = 500.0;

		when(accountRepository.findById(1L)).thenReturn(Optional.of(accountsArray[0]));
		when(accountRepository.save(any(Account.class))).thenReturn(accountsArray[0]);

		ResponseEntity<Object> response = accountServiceImpl.withdraw(1L, withdrawalAmount);

		assertEquals(200, response.getStatusCodeValue());

		Account updatedAccount = (Account) response.getBody();
		assertEquals(1000.0 - withdrawalAmount, updatedAccount.getBalance(), 0.0);  // Saldo actualizado: 500

		verify(accountRepository, times(1)).findById(1L);
		verify(accountRepository, times(1)).save(any(Account.class));
	}

	@Test
	public void testWithdraw_InvalidAmount() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			accountServiceImpl.withdraw(1L, -100.0);
		});
		assertEquals("Withdrawal amount must be positive", thrown.getMessage());

		thrown = assertThrows(IllegalArgumentException.class, () -> {
			accountServiceImpl.withdraw(1L, 0.0);
		});
		assertEquals("Withdrawal amount must be positive", thrown.getMessage());
	}

	@Test
	public void testDeposit_SuccessfulDeposit() {
		Long accountId = 1L;
		double depositAmount = 500.0;
		double initialBalance = 1000.0;
		double expectedBalance = initialBalance + depositAmount;

		Account accountToDeposit = accountsArray[0];

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountToDeposit));
		when(accountRepository.save(any(Account.class))).thenReturn(accountToDeposit);

		ResponseEntity<Object> response = accountServiceImpl.deposit(accountId, depositAmount);

		assertEquals(200, response.getStatusCodeValue());
		Account result = (Account) response.getBody();
		assertAccountBalance(result, expectedBalance);

		verify(accountRepository, times(1)).save(accountToDeposit);
	}

	@Test
	public void testDeposit_InvalidAmount() {
		Long accountId = 1L;
		double negativeDepositAmount = -100.0;

		Account accountToDeposit = accountsArray[0];
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountToDeposit));

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			accountServiceImpl.deposit(accountId, negativeDepositAmount);
		});
		assertEquals("Deposit amount must be positive", thrown.getMessage());

		verify(accountRepository, times(0)).save(any(Account.class));
	}

	@Test
	public void testDeleteAccount_Success() {
		when(accountRepository.findById(1L)).thenReturn(Optional.of(accountsArray[0]));

		ResponseEntity<Object> response = accountServiceImpl.deleteAccount(1L);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Account successfully deleted", response.getBody());

		verify(accountRepository, times(1)).deleteById(1L);
	}

	@Test
	public void testDeleteAccount_NotFound() {
		when(accountRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseEntity<Object> response = accountServiceImpl.deleteAccount(1L);

		assertEquals(404, response.getStatusCodeValue());
		assertEquals("Account not found", response.getBody());

		verify(accountRepository, never()).deleteById(1L);
	}
}
