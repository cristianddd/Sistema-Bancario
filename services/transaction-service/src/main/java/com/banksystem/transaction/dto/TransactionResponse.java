package com.banksystem.transaction.dto;

import com.banksystem.transaction.model.TransactionStatus;
import com.banksystem.transaction.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String accountId;
    private String targetAccountId;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;

}