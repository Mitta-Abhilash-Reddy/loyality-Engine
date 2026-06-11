package com.loyalty.config;

import com.loyalty.customer.CustomerRepository;
import com.loyalty.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "customers", customerRepository.count(),
            "transactions", transactionRepository.count(),
            "message", "Loyalty Engine is running!"
        );
    }
}
