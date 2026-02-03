package com.example.accountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class RestFraudCheckClient implements FraudCheckClient {

    private static final Logger log = LoggerFactory.getLogger(RestFraudCheckClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final String baseUrl;

    public RestFraudCheckClient(@Value("${fraudcheck.url:http://localhost:9080}") String baseUrl) {
        this.baseUrl = baseUrl;
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
        String url = String.format("%s/api/fraud/%s?accountNumber=%s&amount=%s", baseUrl, operation, accountNumber, amount);
        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            Boolean allowed = response.getBody();
            log.debug("Fraud service responded {} for {} {}", allowed, operation, amount);
            return allowed != null && allowed;
        } catch (RestClientException ex) {
            log.error("Erro ao chamar serviço de fraude: {}", ex.getMessage());
            // In case of failure, default to allow to avoid blocking core functionality
            return true;
        }
    }
}