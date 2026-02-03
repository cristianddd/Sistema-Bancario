package com.example.accountservice.service;

import com.example.accountservice.dto.AccountRequest;
import com.example.accountservice.dto.AccountResponse;
import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.exception.AccountNotFoundException;
import com.example.accountservice.exception.InsufficientFundsException;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;


@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final FraudCheckClient fraudCheckClient;

    public AccountService(AccountRepository accountRepository, FraudCheckClient fraudCheckClient) {
        this.accountRepository = accountRepository;
        this.fraudCheckClient = fraudCheckClient;
    }

    
    @Timed(value = "account.create.time", description = "Tempo gasto para criar uma conta")
    @Counted(value = "account.create.count", description = "Número de contas criadas")
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        BigDecimal initial = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;
        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .ownerName(request.getOwnerName())
                .balance(initial)
                .build();
        Account saved = accountRepository.save(account);
        log.info("Conta {} criada para {} com saldo inicial {}", saved.getAccountNumber(), saved.getOwnerName(), saved.getBalance());
        return toResponse(saved);
    }

    
    @Timed(value = "account.get.time", description = "Tempo gasto para buscar conta")
    @Counted(value = "account.get.count", description = "Número de buscas de conta")
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        return toResponse(findOrThrow(accountNumber));
    }

    
    @Timed(value = "account.deposit.time", description = "Tempo gasto para depositar em uma conta")
    @Counted(value = "account.deposit.count", description = "Número de depósitos realizados")
    @Transactional
    public AccountResponse deposit(String accountNumber, TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser maior que zero");
        }
        Account account = findOrThrow(accountNumber);
        // call fraud service before processing deposit
        boolean allowed = fraudCheckClient.validateDeposit(accountNumber, request.getAmount());
        if (!allowed) {
            throw new IllegalStateException("Depósito recusado pelo serviço de fraude");
        }
        account.setBalance(account.getBalance().add(request.getAmount()));
        // flush to DB so subsequent reads see updated balance
        accountRepository.save(account);
        log.info("Depositando {} na conta {}. Novo saldo: {}", request.getAmount(), accountNumber, account.getBalance());
        return toResponse(account);
    }

    
    @Timed(value = "account.withdraw.time", description = "Tempo gasto para sacar de uma conta")
    @Counted(value = "account.withdraw.count", description = "Número de saques realizados")
    @Transactional
    public AccountResponse withdraw(String accountNumber, TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Valor do saque deve ser maior que zero");
        }
        Account account = findOrThrow(accountNumber);
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Saldo insuficiente para saque");
        }
        boolean allowed = fraudCheckClient.validateWithdrawal(accountNumber, request.getAmount());
        if (!allowed) {
            throw new IllegalStateException("Saque recusado pelo serviço de fraude");
        }
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        log.info("Sacando {} da conta {}. Novo saldo: {}", request.getAmount(), accountNumber, account.getBalance());
        return toResponse(account);
    }

    
    private Account findOrThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + accountNumber));
    }

    
    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }
}