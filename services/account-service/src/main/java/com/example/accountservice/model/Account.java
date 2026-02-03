package com.example.accountservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    private String accountNumber;

    
    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }
}