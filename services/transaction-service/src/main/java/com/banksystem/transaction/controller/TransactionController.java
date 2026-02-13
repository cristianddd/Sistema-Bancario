package com.banksystem.transaction.controller;

import com.banksystem.transaction.dto.DepositRequest;
import com.banksystem.transaction.dto.TransactionResponse;
import com.banksystem.transaction.dto.TransferRequest;
import com.banksystem.transaction.dto.WithdrawRequest;
import com.banksystem.transaction.model.Transaction;
import com.banksystem.transaction.model.TransactionStatus;
import com.banksystem.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transactions API", description = "Endpoints for deposits, withdrawals, transfers and transaction queries")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Deposit", description = "Creates a deposit transaction. Requires Idempotency-Key header.")
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Parameter(description = "Idempotency key to prevent duplicate processing", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request
    ) {
        TransactionResponse tx = transactionService.deposit(request, idempotencyKey);
        return new ResponseEntity<>(tx,
                tx.getStatus() == TransactionStatus.SUCCESS ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @Operation(summary = "Withdraw", description = "Creates a withdraw transaction. Requires Idempotency-Key header.")
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Parameter(description = "Idempotency key to prevent duplicate processing", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequest request
    ) {
        TransactionResponse tx = transactionService.withdraw(request, idempotencyKey);
        return new ResponseEntity<>(tx,
                tx.getStatus() == TransactionStatus.SUCCESS ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @Operation(summary = "Transfer", description = "Creates a transfer transaction. Requires Idempotency-Key header.")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Parameter(description = "Idempotency key to prevent duplicate processing", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {
        TransactionResponse tx = transactionService.transfer(request, idempotencyKey);
        return new ResponseEntity<>(tx,
                tx.getStatus() == TransactionStatus.SUCCESS ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @Operation(summary = "List transactions by account", description = "Returns transactions filtered by accountId.")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listByAccount(
            @RequestParam("accountId") String accountId
    ) {
        List<TransactionResponse> responses = transactionService.listByAccount(accountId);
        return ResponseEntity.ok(responses);
    }
}