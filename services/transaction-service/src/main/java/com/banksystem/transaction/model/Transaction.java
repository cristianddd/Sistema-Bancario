package com.banksystem.transaction.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "idempotencyKey")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String accountId;

    private String targetAccountId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private String idempotencyKey;
}