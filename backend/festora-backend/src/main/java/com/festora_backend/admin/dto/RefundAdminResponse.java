package com.festora_backend.admin.dto;

import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundAdminResponse(
        Long refundId,
        UUID bookingReference,
        String customerName,
        String customerEmail,
        String eventTitle,
        BigDecimal amount,
        PaymentStatus status,
        String reason,
        String billingReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static RefundAdminResponse from(RefundAudit audit) {
        return new RefundAdminResponse(
                audit.getId(),
                audit.getBooking().getBookingReference(),
                audit.getBooking().getUser().getFullName(),
                audit.getBooking().getUser().getEmail(),
                audit.getBooking().getEvent().getTitle(),
                audit.getAmount(),
                audit.getStatus(),
                audit.getReason(),
                audit.getBillingReference(),
                audit.getFailureReason(),
                audit.getCreatedAt(),
                audit.getUpdatedAt()
        );
    }
}