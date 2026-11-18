
package com.festora_backend.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record RazorpayOrderResponse(
        UUID bookingReference,
        String razorpayOrderId,
        Long amount,
        String currency,
        String keyId,
        Instant expiresAt
) {
}
