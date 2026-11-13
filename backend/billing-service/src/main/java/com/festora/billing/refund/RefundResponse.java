package com.festora.billing.refund;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
        boolean successful,
        UUID bookingReference,
        String refundReference,
        BigDecimal amount,
        String status,
        String message,
        Instant createdAt
) {
    public static RefundResponse from(Refund refund) {
        return new RefundResponse(
                refund.getStatus() == RefundStatus.SUCCEEDED,
                refund.getBookingReference(),
                refund.getRefundReference(),
                refund.getAmount(),
                refund.getStatus().name(),
                "Refund recorded successfully",
                refund.getCreatedAt()
        );
    }
}