package com.festora_backend.booking.dto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record BookingSummaryResponse(
        UUID bookingReference,
        Long eventId,
        String eventTitle,
        Instant eventStartAt,
        Integer quantity,
        BigDecimal totalAmount,
        String status,
        Instant createdAt
) {
}