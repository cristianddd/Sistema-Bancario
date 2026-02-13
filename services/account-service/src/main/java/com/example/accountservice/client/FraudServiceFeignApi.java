package com.example.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(
        name = "fraud-service-api",
        url = "${fraudcheck.url:http://localhost:9080}"
)
public interface FraudServiceFeignApi {

    @GetMapping("/api/fraud/{operation}")
    Boolean validate(
            @PathVariable("operation") String operation,
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("amount") BigDecimal amount
    );
}
