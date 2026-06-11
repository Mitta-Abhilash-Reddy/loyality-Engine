package com.loyalty.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions
     * Earn points for a purchase. Doubles on weekends automatically.
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> earnPoints(@Valid @RequestBody EarnRequest request) {
        return ResponseEntity.ok(transactionService.earnPoints(request));
    }

    /**
     * POST /api/transactions/redeem
     * Redeem points. Validates sufficient balance before deducting.
     */
    @PostMapping("/redeem")
    public ResponseEntity<TransactionDTO> redeemPoints(@Valid @RequestBody RedeemRequest request) {
        return ResponseEntity.ok(transactionService.redeemPoints(request));
    }

    /**
     * GET /api/transactions/{customerId}
     * Transaction history with optional date range filter and pagination.
     *
     * Query params:
     *   from  — ISO datetime, e.g. 2024-01-01T00:00:00
     *   to    — ISO datetime, e.g. 2024-12-31T23:59:59
     *   page  — page index (default 0)
     *   size  — page size (default 20)
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<Page<TransactionDTO>> getHistory(
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getTransactions(customerId, from, to, pageable));
    }
}
