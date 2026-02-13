package com.banksystem.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "O valor deve ser maior que zero")
    private BigDecimal amount;

}