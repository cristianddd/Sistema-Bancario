package com.example.accountservice.service;

import com.example.accountservice.dto.AccountRequest;
import com.example.accountservice.dto.AccountResponse;
import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.exception.AccountNotFoundException;
import com.example.accountservice.exception.InsufficientFundsException;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FraudCheckClient fraudCheckClient;

    @InjectMocks
    private AccountService accountService;

    private Account existingAccount;

    @BeforeEach
    void setUp() {
        existingAccount = Account.builder()
                .id(1L)
                .accountNumber("123")
                .ownerName("João")
                .balance(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAccount_savesAndReturnsResponse() {
        AccountRequest request = new AccountRequest();
        request.setAccountNumber("789");
        request.setOwnerName("Maria");
        request.setInitialBalance(new BigDecimal("50.00"));
        Account saved = Account.builder()
                .id(2L)
                .accountNumber("789")
                .ownerName("Maria")
                .balance(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.getAccountNumber()).isEqualTo("789");
        assertThat(response.getBalance()).isEqualByComparingTo("50.00");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAccountByNumber_returnsAccountResponse_whenFound() {
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(existingAccount));
        AccountResponse response = accountService.getAccountByNumber("123");
        assertThat(response.getOwnerName()).isEqualTo("João");
    }

    @Test
    void getAccountByNumber_throws_whenNotFound() {
        when(accountRepository.findByAccountNumber("404")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByNumber("404"));
    }

    @Test
    void deposit_addsBalance_whenAllowedByFraudService() {
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(existingAccount));
        when(fraudCheckClient.validateDeposit("123", new BigDecimal("25.00"))).thenReturn(true);
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("25.00"));

        AccountResponse response = accountService.deposit("123", request);

        assertThat(response.getBalance()).isEqualByComparingTo("125.00");
    }

    @Test
    void deposit_throws_whenAmountNegative() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("-5.00"));
        assertThrows(IllegalArgumentException.class, () -> accountService.deposit("123", request));
    }

    @Test
    void withdraw_subtractsBalance_whenSufficientFundsAndAllowed() {
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(existingAccount));
        when(fraudCheckClient.validateWithdrawal("123", new BigDecimal("40.00"))).thenReturn(true);
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("40.00"));

        AccountResponse response = accountService.withdraw("123", request);
        assertThat(response.getBalance()).isEqualByComparingTo("60.00");
    }

    @Test
    void withdraw_throws_whenInsufficientFunds() {
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(existingAccount));
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        assertThrows(InsufficientFundsException.class, () -> accountService.withdraw("123", request));
    }
}