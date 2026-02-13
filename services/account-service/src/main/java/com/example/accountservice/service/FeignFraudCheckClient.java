package com.example.accountservice.service;

import com.example.accountservice.client.FraudServiceFeignApi;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FeignFraudCheckClient implements FraudCheckClient {

    private static final Logger log = LoggerFactory.getLogger(FeignFraudCheckClient.class);

    private final FraudServiceFeignApi fraudServiceFeignApi;
    private final boolean failOpen;

    public FeignFraudCheckClient(
            FraudServiceFeignApi fraudServiceFeignApi,
            @Value("${fraudcheck.fail-open:false}") boolean failOpen
    ) {
        this.fraudServiceFeignApi = fraudServiceFeignApi;
        this.failOpen = failOpen;
    }

    @Override
    public boolean validateDeposit(String accountNumber, BigDecimal amount) {
        return callFraudService("deposit", accountNumber, amount);
    }

    @Override
    public boolean validateWithdrawal(String accountNumber, BigDecimal amount) {
        return callFraudService("withdraw", accountNumber, amount);
    }

    private boolean callFraudService(String operation, String accountNumber, BigDecimal amount) {
        try {
            Boolean allowed = fraudServiceFeignApi.validate(operation, accountNumber, amount);
            log.debug("Fraud service responded {} for {} {}", allowed, operation, amount);
            return allowed != null && allowed;
        } catch (FeignException ex) {
            log.error("Erro ao chamar serviço de fraude via Feign. Política failOpen={}", failOpen, ex);
            return failOpen;
        }
    }
}
