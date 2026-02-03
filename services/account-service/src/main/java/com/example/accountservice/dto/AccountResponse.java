package com.example.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class AccountResponse {
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public AccountResponse(String accountNumber, String ownerName, BigDecimal balance, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}