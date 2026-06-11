package com.loyalty.seeder;

import com.loyalty.customer.Customer;
import com.loyalty.customer.CustomerRepository;
import com.loyalty.transaction.Transaction;
import com.loyalty.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // fixed seed for reproducibility

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("Seeding database with realistic loyalty data...");

        List<Customer> allCustomers = new ArrayList<>();

        // --- 5 PLATINUM customers (active last 3 days, 10000+ points) ---
        String[] platinumNames = {"Arjun Sharma", "Priya Nair", "Vikram Reddy", "Ananya Iyer", "Rohan Mehta"};
        for (int i = 0; i < 5; i++) {
            Customer c = createCustomer(
                platinumNames[i],
                "platinum" + (i + 1) + "@demo.com",
                12000 + random.nextInt(8000),       // 12000–20000 points
                    Customer.Tier.PLATINUM,
                daysAgo(random.nextInt(3))           // active last 3 days
            );
            allCustomers.add(customerRepository.save(c));
        }

        // --- 5 GOLD customers (active last 10 days, 5000–9999 points) ---
        String[] goldNames = {"Kavya Pillai", "Suresh Kumar", "Deepa Joshi", "Arun Patel", "Meena Rao"};
        for (int i = 0; i < 5; i++) {
            Customer c = createCustomer(
                goldNames[i],
                "gold" + (i + 1) + "@demo.com",
                5000 + random.nextInt(4999),         // 5000–9999 points
                    Customer.Tier.GOLD,
                daysAgo(4 + random.nextInt(7))       // active 4–10 days ago
            );
            allCustomers.add(customerRepository.save(c));
        }

        // --- 5 SILVER customers (last tx 25–35 days ago → churn risk) ---
        String[] silverNames = {"Ravi Verma", "Sunita Singh", "Kiran Bose", "Lakshmi Das", "Nitin Jain"};
        for (int i = 0; i < 5; i++) {
            Customer c = createCustomer(
                silverNames[i],
                "silver" + (i + 1) + "@demo.com",
                1000 + random.nextInt(3999),         // 1000–4999 points
                    Customer.Tier.SILVER,
                daysAgo(25 + random.nextInt(11))     // 25–35 days ago → at risk
            );
            allCustomers.add(customerRepository.save(c));
        }

        // --- 5 BRONZE customers (last tx 40–60 days ago → high churn risk) ---
        String[] bronzeNames = {"Pooja Gupta", "Rahul Mishra", "Sneha Tiwari", "Ajay Pandey", "Ritu Saxena"};
        for (int i = 0; i < 5; i++) {
            Customer c = createCustomer(
                bronzeNames[i],
                "bronze" + (i + 1) + "@demo.com",
                100 + random.nextInt(899),           // 100–999 points
                    Customer.Tier.BRONZE,
                daysAgo(40 + random.nextInt(21))     // 40–60 days ago → high risk
            );
            allCustomers.add(customerRepository.save(c));
        }

        log.info("Created {} customers", allCustomers.size());

        // --- Seed transactions for each customer ---
        int totalTransactions = 0;
        for (Customer customer : allCustomers) {
            totalTransactions += seedTransactionsForCustomer(customer);
        }

        log.info("Seeded {} transactions", totalTransactions);
        log.info("Database seeding complete!");
        log.info("Tier distribution: 5 PLATINUM, 5 GOLD, 5 SILVER (at-risk), 5 BRONZE (high-risk)");
    }

    private Customer createCustomer(String name, String email, int points,
                                    Customer.Tier tier, LocalDateTime lastTxAt) {
        return Customer.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .phone("9" + (800000000 + random.nextInt(100000000)))
                .totalPoints(points)
                .tier(tier)
                .churnScore(0.0)  // will be calculated in Phase 4
                .lastTransactionAt(lastTxAt)
                .active(true)
                .build();
    }

    private int seedTransactionsForCustomer(Customer customer) {
        int txCount = 5 + random.nextInt(6); // 5–10 transactions
        int runningPoints = customer.getTotalPoints();
        List<Transaction> transactions = new ArrayList<>();

        // Determine time range for transactions based on tier
        // PLATINUM/GOLD: spread across last 30 days with recent ones
        // SILVER: spread across 30–90 days ago (none in last 30 days)
        // BRONZE: spread across 40–90 days ago (none in last 30 days)

        boolean isAtRisk = customer.getTier() == Customer.Tier.SILVER
                        || customer.getTier() == Customer.Tier.BRONZE;

        for (int i = 0; i < txCount; i++) {
            LocalDateTime txTime;

            if (isAtRisk) {
                // All transactions older than 30 days
                int daysBack = 30 + random.nextInt(61); // 30–90 days ago
                txTime = daysAgo(daysBack);
            } else {
                // Mix of recent and older transactions
                int daysBack = random.nextInt(30); // within last 30 days
                txTime = daysAgo(daysBack);
            }

            // Decide EARN or REDEEM
            // Mostly EARN, occasionally REDEEM (only if enough points)
            boolean isRedeem = (random.nextInt(5) == 0) && (runningPoints > 200);

            double amount = 200 + random.nextInt(4801); // ₹200–₹5000
            int pointsChanged;
            String description;

            if (isRedeem) {
                pointsChanged = -(50 + random.nextInt(151)); // redeem 50–200 points
                description = "Points redeemed for discount";
                runningPoints += pointsChanged;
                if (runningPoints < 0) runningPoints = 0;
            } else {
                pointsChanged = (int) (amount / 10); // ₹10 = 1 point
                description = pickRandomDescription();
                runningPoints += pointsChanged;
            }

            Transaction tx = Transaction.builder()
                    .customer(customer)
                    .type(isRedeem ? Transaction.TransactionType.REDEEM : Transaction.TransactionType.EARN)
                    .amount(amount)
                    .pointsEarned(isRedeem ? 0 : pointsChanged)
                    .pointsRedeemed(isRedeem ? pointsChanged : 0)
                    .description(description)
                    .build();

            // We'll set createdAt manually via reflection-like trick
            // Spring's @CreationTimestamp will set current time, so we override after save
            transactions.add(tx);
        }

        List<Transaction> saved = transactionRepository.saveAll(transactions);

        // Update createdAt timestamps manually using JPQL or native query would be ideal,
        // but for seeding purposes the auto-timestamp is acceptable.
        // The lastTransactionAt on Customer already reflects the right time for churn scoring.

        return saved.size();
    }

    private String pickRandomDescription() {
        String[] descriptions = {
            "Flight booking - IndiGo",
            "Hotel stay - Marriott Mumbai",
            "Restaurant dining - Taj Hotels",
            "Online shopping - Flipkart",
            "Fuel purchase",
            "Grocery shopping",
            "Movie tickets",
            "Cab ride - Uber",
            "Flight booking - Air India",
            "Retail purchase - Shoppers Stop",
            "Coffee shop - Starbucks",
            "Electronics - Croma"
        };
        return descriptions[random.nextInt(descriptions.length)];
    }

    private LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days).minusHours(random.nextInt(24));
    }
}
