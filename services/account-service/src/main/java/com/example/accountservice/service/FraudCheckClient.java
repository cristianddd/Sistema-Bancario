package com.example.accountservice.service;

import java.math.BigDecimal;


public interface FraudCheckClient {
    
    boolean validateDeposit(String accountNumber, BigDecimal amount);

    
    boolean validateWithdrawal(String accountNumber, BigDecimal amount);
}