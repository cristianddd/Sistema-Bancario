package com.example.accountservice.controller;

import com.example.accountservice.dto.AccountRequest;
import com.example.accountservice.dto.AccountResponse;
import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
@Validated
@Tag(name = "Contas", description = "Operações para criar e gerenciar contas bancárias")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Criar uma nova conta")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        log.debug("Recebendo requisição para criar conta: {}", request);
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @Operation(summary = "Buscar detalhes de uma conta pelo número")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @Operation(summary = "Depositar valor em uma conta")
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable String accountNumber,
                                                  @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, request));
    }

    @Operation(summary = "Sacar valor de uma conta")
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountNumber,
                                                   @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(accountService.withdraw(accountNumber, request));
    }
}