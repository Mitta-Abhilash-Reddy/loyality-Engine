package com.loyalty.transaction;

import com.loyalty.churn.ChurnScoringService;
import com.loyalty.customer.Customer;
import com.loyalty.customer.CustomerNotFoundException;
import com.loyalty.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ChurnScoringService churnScoringService;

    @Value("${loyalty.points.rate:10}")
    private int pointsRate;

    // ── Earn Points ──────────────────────────────────────────────────────────

    @Transactional
    public TransactionDTO earnPoints(EarnRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        boolean isWeekend = isWeekend();
        int basePoints = (int) (request.getAmount() / pointsRate);
        int pointsEarned = isWeekend ? basePoints * 2 : basePoints;

        customer.setTotalPoints(customer.getTotalPoints() + pointsEarned);
        customer.setLastTransactionAt(LocalDateTime.now());
        updateTier(customer);
        customerRepository.save(customer);

        Transaction tx = Transaction.builder()
                .customer(customer)
                .amount(request.getAmount())
                .type(Transaction.TransactionType.EARN)
                .pointsEarned(pointsEarned)
                .pointsRedeemed(0)
                .weekendBonus(isWeekend)
                .description(request.getDescription() != null
                        ? request.getDescription()
                        : "Points earned for ₹" + request.getAmount())
                .build();

        Transaction saved = transactionRepository.save(tx);

        churnScoringService.scoreAndUpdateAsync(customer.getId());

        return TransactionDTO.from(saved);
    }

    // ── Redeem Points ────────────────────────────────────────────────────────

    @Transactional
    public TransactionDTO redeemPoints(RedeemRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        if (customer.getTotalPoints() < request.getPoints()) {
            throw new InsufficientPointsException(
                    "Insufficient points. Available: " + customer.getTotalPoints()
                            + ", Requested: " + request.getPoints());
        }

        customer.setTotalPoints(customer.getTotalPoints() - request.getPoints());
        customer.setLastTransactionAt(LocalDateTime.now());
        updateTier(customer);
        customerRepository.save(customer);

        Transaction tx = Transaction.builder()
                .customer(customer)
                .amount(0.0)
                .type(Transaction.TransactionType.REDEEM)
                .pointsEarned(0)
                .pointsRedeemed(request.getPoints())
                .weekendBonus(false)
                .description(request.getDescription() != null
                        ? request.getDescription()
                        : "Points redeemed: " + request.getPoints())
                .build();

        Transaction saved = transactionRepository.save(tx);

        churnScoringService.scoreAndUpdateAsync(customer.getId());

        return TransactionDTO.from(saved);
    }

    // ── Query ────────────────────────────────────────────────────────────────

    public Page<TransactionDTO> getTransactions(Long customerId,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                Pageable pageable) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + customerId));

        Page<Transaction> page;
        if (from != null && to != null) {
            page = transactionRepository.findByCustomerIdAndDateRange(
                    customerId, from, to, pageable);
        } else {
            page = transactionRepository.findByCustomerIdOrderByCreatedAtDesc(
                    customerId, pageable);
        }

        return page.map(TransactionDTO::from);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void updateTier(Customer customer) {
        int pts = customer.getTotalPoints();
        if (pts >= 10000) {
            customer.setTier(Customer.Tier.PLATINUM);
        } else if (pts >= 5000) {
            customer.setTier(Customer.Tier.GOLD);
        } else if (pts >= 1000) {
            customer.setTier(Customer.Tier.SILVER);
        } else {
            customer.setTier(Customer.Tier.BRONZE);
        }
    }

    private boolean isWeekend() {
        DayOfWeek day = LocalDateTime.now().getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}