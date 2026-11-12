package com.festora_backend.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillingRefundRequest(
        UUID bookingReference,
        BigDecimal amount,
        String reason
) {
}