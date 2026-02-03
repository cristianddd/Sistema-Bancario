package com.example.accountservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class TransactionRequest {
    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "O valor deve ser maior que zero")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}