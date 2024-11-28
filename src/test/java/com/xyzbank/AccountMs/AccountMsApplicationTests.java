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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AccountMsApplicationTests {

	@Mock
	private AccountRepository accountRepository; // Mock del repositorio
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

		accountsArray = new Account[] {
				new Account(Account.AccountType.SAVINGS, 1L),
				new Account(Account.AccountType.CHECKING, 2L),
				new Account(Account.AccountType.SAVINGS, 3L)
		};

		accountsArray[0].setBalance(1000.0);
		accountsArray[1].setBalance(1500.0);
		accountsArray[2].setBalance(2000.0);

		when(accountRepository.findById(1L)).thenReturn(Optional.of(accountsArray[0]));
		when(accountRepository.findById(2L)).thenReturn(Optional.of(accountsArray[1]));
		when(accountRepository.findById(3L)).thenReturn(Optional.of(accountsArray[2]));

		when(accountRepository.save(any(Account.class))).thenReturn(accountsArray[0]);
		when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

		accountController = new AccountController(accountService);

		// Configuramos el comportamiento del servicio para el método getAllAccounts
		Mockito.when(accountService.getAllAccounts()).thenReturn(Arrays.asList(
				new Account(Account.AccountType.SAVINGS, 1L),
				new Account(Account.AccountType.CHECKING, 2L),
				new Account(Account.AccountType.SAVINGS, 3L)
		));
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
	public void testGetAllAccounts() {
		// Realizamos la aserción directamente en el resultado de la llamada al método del controlador
		List<Account> accounts = accountController.getAllAccounts();  // Invocamos el método del controlador
		assertNotNull(accounts);  // Verifica que la lista no sea nula
		assertEquals(3, accounts.size());  // Verifica que haya 3 cuentas

		// Verificar que el servicio haya sido invocado correctamente
		Mockito.verify(accountService, Mockito.times(1)).getAllAccounts();
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
	public void testWithdraw_SuccessfulWithdrawal() {
		ResponseEntity<Object> response = accountServiceImpl.withdraw(1L, 500.0);
		assertEquals(200, response.getStatusCodeValue());
		Account updatedAccount = (Account) response.getBody();
		assertNotNull(updatedAccount);
		assertEquals(500.0, updatedAccount.getBalance(), 0.0);
	}

	@Test
	public void testDeposit_SuccessfulDeposit() {
		// Datos para la prueba
		Long accountId = 1L;
		double depositAmount = 500.0;
		double initialBalance = 1000.0;
		double expectedBalance = initialBalance + depositAmount;  // Balance esperado después del depósito

		// Preparamos la cuenta con un saldo inicial
		Account accountToDeposit = accountsArray[0]; // cuenta con saldo inicial 1000.0

		// Simulamos la respuesta del repositorio al buscar la cuenta por ID
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountToDeposit));
		// Simulamos el comportamiento de guardar la cuenta actualizada en el repositorio
		when(accountRepository.save(any(Account.class))).thenReturn(accountToDeposit);

		// Llamamos al servicio de depósito
		ResponseEntity<Object> response = accountServiceImpl.deposit(accountId, depositAmount);

		// Verificamos que la respuesta sea correcta (código 200 y saldo actualizado)
		assertEquals(200, response.getStatusCodeValue());  // Aseguramos que el estado de la respuesta sea OK
		Account result = (Account) response.getBody();
		assertNotNull(result);  // Aseguramos que el cuerpo de la respuesta no sea nulo
		assertEquals(expectedBalance, result.getBalance(), 0.0);  // Comprobamos que el saldo actualizado es el esperado

		// Verificamos que el repositorio haya guardado la cuenta actualizada
		verify(accountRepository, times(1)).save(accountToDeposit);
	}

	@Test
	public void testDeleteAccount_Success() {
		// Mock de cuenta encontrada
		when(accountRepository.findById(1L)).thenReturn(Optional.of(accountsArray[0]));

		// Llamamos al servicio para eliminar la cuenta
		ResponseEntity<Object> response = accountServiceImpl.deleteAccount(1L);

		// Verificamos que la respuesta sea correcta: código 200 y mensaje de éxito
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Account successfully deleted", response.getBody());

		// Verificamos que el método deleteById fue llamado exactamente una vez
		verify(accountRepository, times(1)).deleteById(1L);
	}

	// Nuevo test: verificamos el caso en que la cuenta no se encuentra (y se devuelve 404)
	@Test
	public void testDeleteAccount_NotFound() {
		// Mock de cuenta no encontrada
		when(accountRepository.findById(1L)).thenReturn(Optional.empty());

		// Llamamos al servicio para eliminar la cuenta
		ResponseEntity<Object> response = accountServiceImpl.deleteAccount(1L);

		// Verificamos que la respuesta sea correcta: código 404 y mensaje de "Account not found"
		assertEquals(404, response.getStatusCodeValue());
		assertEquals("Account not found", response.getBody());

		// Verificamos que el método deleteById NO fue llamado
		verify(accountRepository, never()).deleteById(1L);
	}
}