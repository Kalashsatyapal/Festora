package com.festora_backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
        Long refundId,
        UUID bookingReference,
        BigDecimal amount,
        String status,
        String reason,
        String billingReference,
        Instant createdAt,
        Instant updatedAt
) {
}