package com.banksystem.transaction.client;

import com.banksystem.transaction.dto.TransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(
        name = "account-service",
        url = "${services.account.base-url:http://bank-account-service:8080}"
)
public interface AccountClient {

    @GetMapping("/accounts/{accountId}/balance")
    BigDecimal getBalance(@PathVariable("accountId") String accountId);

    @PostMapping("/accounts/{accountId}/debit")
    void debit(
            @PathVariable("accountId") String accountId,
            @RequestBody TransactionRequest body
    );

    @PostMapping("/accounts/{accountId}/credit")
    void credit(
            @PathVariable("accountId") String accountId,
            @RequestBody TransactionRequest body
    );
}