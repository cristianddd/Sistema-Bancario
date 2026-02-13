package com.banksystem.transaction.service;

import com.banksystem.transaction.client.AccountClient;
import com.banksystem.transaction.dto.*;
import com.banksystem.transaction.model.Transaction;
import com.banksystem.transaction.model.TransactionStatus;
import com.banksystem.transaction.model.TransactionType;
import com.banksystem.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;

    @Transactional
    public TransactionResponse deposit(DepositRequest request, String idempotencyKey) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return buildTransactionResponse(existing.get());
        }
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Transaction transaction = new Transaction(null,
                request.getAccountId(),
                null,
                amount,
                TransactionType.DEPOSIT,
                TransactionStatus.PENDING,
                LocalDateTime.now(),
                idempotencyKey
        );
        transactionRepository.save(transaction);
        try {
            TransactionRequest transactionRequest = new TransactionRequest(amount);
            accountClient.credit(request.getAccountId(), transactionRequest);
            transaction.setStatus(TransactionStatus.SUCCESS);
        } catch (Exception ex) {
            logger.error("Failed to process deposit: {}", ex.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            throw ex;
        }
        return buildTransactionResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, String idempotencyKey) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return buildTransactionResponse(existing.get());
        }
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        // validate sufficient funds
        BigDecimal balance = accountClient.getBalance(request.getAccountId());
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }
        Transaction transaction = new Transaction(null,
                request.getAccountId(),
                null,
                amount,
                TransactionType.WITHDRAW,
                TransactionStatus.PENDING,
                LocalDateTime.now(),
                idempotencyKey
        );
        transactionRepository.save(transaction);
        try {
            TransactionRequest transactionRequest = new TransactionRequest(amount);
            accountClient.debit(request.getAccountId(), transactionRequest);
            transaction.setStatus(TransactionStatus.SUCCESS);
        } catch (Exception ex) {
            logger.error("Failed to process withdrawal: {}", ex.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            throw ex;
        }
        return buildTransactionResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String idempotencyKey) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return buildTransactionResponse(existing.get());
        }
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (request.getAccountId().equals(request.getTargetAccountId())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }
        BigDecimal balance = accountClient.getBalance(request.getAccountId());
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }
        Transaction transaction = new Transaction(null, request.getAccountId(), request.getTargetAccountId(),
                amount, TransactionType.TRANSFER, TransactionStatus.PENDING, LocalDateTime.now(), idempotencyKey
        );
        transactionRepository.save(transaction);
        try {
            TransactionRequest transactionRequest = new TransactionRequest(amount);
            accountClient.debit(request.getAccountId(), transactionRequest);
            accountClient.credit(request.getTargetAccountId(), transactionRequest);
            transaction.setStatus(TransactionStatus.SUCCESS);
        } catch (Exception ex) {
            logger.error("Failed to process transfer: {}", ex.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            throw ex;
        }
        return buildTransactionResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listByAccount(String accountId) {
        return transactionRepository.findAll()
                .stream()
                .filter(t -> t.getAccountId().equals(accountId) || accountId.equals(t.getTargetAccountId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::buildTransactionResponse)
                .toList();
    }

    public TransactionResponse buildTransactionResponse(Transaction tx) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(tx.getId());
        transactionResponse.setAccountId(tx.getAccountId());
        transactionResponse.setTargetAccountId(tx.getTargetAccountId());
        transactionResponse.setType(tx.getType());
        transactionResponse.setStatus(tx.getStatus());
        transactionResponse.setAmount(tx.getAmount());
        transactionResponse.setCreatedAt(tx.getCreatedAt());
        return transactionResponse;
    }
}