package com.loyalty.churn;

import com.loyalty.customer.Customer;
import com.loyalty.customer.CustomerRepository;
import com.loyalty.offer.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChurnScoringService {

    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;
    private final OfferService offerService;

    @Value("${churn.service.url}")
    private String churnServiceUrl;

    @Async
    @Transactional
    public void scoreAndUpdateAsync(Long customerId) {
        try {
            // Wait for TransactionService to commit first
            Thread.sleep(500);

            Optional<Customer> optCustomer = customerRepository.findById(customerId);
            if (optCustomer.isEmpty()) {
                log.warn("Customer {} not found for churn scoring", customerId);
                return;
            }

            Customer customer = optCustomer.get();

            // Build request
            int daysSinceLast = (int)  (customer.getLastTransactionAt() == null ? 999L :
                    ChronoUnit.DAYS.between(customer.getLastTransactionAt(), LocalDateTime.now()));

            boolean activeLastSevenDays = customer.getLastTransactionAt() != null &&
                    ChronoUnit.DAYS.between(customer.getLastTransactionAt(), LocalDateTime.now()) <= 7;

            ChurnScoreRequest request = ChurnScoreRequest.builder()
                    .customerId(customerId)
                    .daysSinceLastTransaction(daysSinceLast)
                    .totalPoints(customer.getTotalPoints())
                    .tier(customer.getTier().name())
                    .activeLastSevenDays(activeLastSevenDays)
                    .build();

            // Call Python churn service
            ChurnScoreResponse response = restTemplate.postForObject(
                    churnServiceUrl, request, ChurnScoreResponse.class);

            if (response != null) {
                customerRepository.updateChurnScore(customerId, response.getChurnScore());
                log.info("Churn score updated for customer {}: {} ({})",
                        customerId, response.getChurnScore(), response.getRiskLevel());

                // Auto-assign offer if churn score is high
                offerService.assignOfferIfEligible(customerId);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Churn scoring interrupted for customer {}", customerId);
        } catch (Exception e) {
            log.warn("Churn scoring failed for customer {} — skipping: {}", customerId, e.getMessage());
        }
    }
}
