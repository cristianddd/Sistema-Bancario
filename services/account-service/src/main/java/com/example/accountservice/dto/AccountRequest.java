package com.example.accountservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;


public class AccountRequest {

    
    @NotBlank(message = "O número da conta é obrigatório")
    @Size(max = 50, message = "O número da conta deve ter no máximo 50 caracteres")
    private String accountNumber;

    
    @NotBlank(message = "O nome do proprietário é obrigatório")
    @Size(max = 120, message = "O nome do proprietário deve ter no máximo 120 caracteres")
    private String ownerName;

    
    @DecimalMin(value = "0.00", inclusive = true, message = "O saldo inicial não pode ser negativo")
    private BigDecimal initialBalance;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}