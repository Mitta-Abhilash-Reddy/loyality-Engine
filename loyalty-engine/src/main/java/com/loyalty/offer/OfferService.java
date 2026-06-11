package com.loyalty.offer;

import com.loyalty.customer.Customer;
import com.loyalty.customer.CustomerNotFoundException;
import com.loyalty.customer.CustomerRepository;
import com.loyalty.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public OfferService(OfferRepository offerRepository,
                        CustomerRepository customerRepository,
                        @Lazy NotificationService notificationService) {
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public OfferDTO assignOffer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
        return assignOfferInternal(customer);
    }

    @Transactional
    public void assignOfferIfEligible(Long customerId) {
        customerRepository.findById(customerId).ifPresent(customer -> {
            try {
                OfferDTO result = assignOfferInternal(customer);
                if (result != null) {
                    log.info("Offer auto-assigned to customer {}: {}", customerId, result.getTitle());
                }
            } catch (Exception e) {
                log.warn("Offer assignment failed for customer {}: {}", customerId, e.getMessage());
            }
        });
    }

    private OfferDTO assignOfferInternal(Customer customer) {
        // Skip if customer already has an active offer
        Optional<Offer> existing = offerRepository.findFirstByCustomerIdAndStatus(
                customer.getId(), Offer.OfferStatus.ACTIVE);
        if (existing.isPresent()) {
            log.info("Customer {} already has an active offer — skipping", customer.getId());
            return null;
        }

        if (customer.getChurnScore() < 0.7) {
            log.info("Customer {} churn score {} below threshold — no offer", customer.getId(), customer.getChurnScore());
            return null;
        }

        Offer offer = buildOffer(customer);
        if (offer == null) return null;

        Offer saved = offerRepository.save(offer);
        log.info("Offer assigned to customer {} ({}): {}", customer.getId(), customer.getTier(), saved.getTitle());

        // Trigger async notification
        notificationService.sendOfferNotificationAsync(customer.getId(), saved);

        return OfferDTO.from(saved);
    }

    private Offer buildOffer(Customer customer) {
        Customer.Tier tier = customer.getTier();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusDays(30);

        return switch (tier) {
            case BRONZE -> Offer.builder()
                    .customer(customer)
                    .title("Double Points This Weekend")
                    .description("Earn 2x points on all purchases this weekend. Don't miss out!")
                    .offerType(Offer.OfferType.DOUBLE_POINTS)
                    .status(Offer.OfferStatus.ACTIVE)
                    .assignedAt(now)
                    .expiresAt(expires)
                    .build();
            case SILVER -> Offer.builder()
                    .customer(customer)
                    .title("500 Bonus Points on Next Purchase")
                    .description("Get 500 bonus points on your very next transaction. Valid for 30 days.")
                    .offerType(Offer.OfferType.BONUS_POINTS)
                    .status(Offer.OfferStatus.ACTIVE)
                    .assignedAt(now)
                    .expiresAt(expires)
                    .build();
            case GOLD, PLATINUM -> Offer.builder()
                    .customer(customer)
                    .title("Exclusive Lounge Access Offer")
                    .description("Complimentary access to our premium lounge. Present this offer at the counter.")
                    .offerType(Offer.OfferType.LOUNGE_ACCESS)
                    .status(Offer.OfferStatus.ACTIVE)
                    .assignedAt(now)
                    .expiresAt(expires)
                    .build();
        };
    }

    public List<OfferDTO> getOffers(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found: " + customerId);
        }
        return offerRepository.findByCustomerIdOrderByAssignedAtDesc(customerId)
                .stream().map(OfferDTO::from).collect(Collectors.toList());
    }

    public List<OfferDTO> getActiveOffers(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found: " + customerId);
        }
        return offerRepository.findByCustomerIdAndStatus(customerId, Offer.OfferStatus.ACTIVE)
                .stream().map(OfferDTO::from).collect(Collectors.toList());
    }
}
