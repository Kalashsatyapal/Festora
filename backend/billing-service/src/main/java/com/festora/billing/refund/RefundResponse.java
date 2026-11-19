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
        String message = switch (refund.getStatus()) {
            case PENDING -> "Refund is being processed by Razorpay";
            case SUCCEEDED -> "Refund processed successfully";
            case FAILED -> "Razorpay could not process the refund";
        };

        return new RefundResponse(
                refund.getStatus() == RefundStatus.SUCCEEDED,
                refund.getBookingReference(),
                refund.getRefundReference(),
                refund.getAmount(),
                refund.getStatus().name(),
                message,
                refund.getCreatedAt()
        );
    }
}