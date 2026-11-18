package com.festora_backend.payment.dto;

import java.util.UUID;

public record PaymentVerificationResponse(
        UUID bookingReference,
        String razorpayPaymentId,
        String paymentStatus,
        String bookingStatus
) {
}