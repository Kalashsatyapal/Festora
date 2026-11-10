package com.festora_backend.booking.dto;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CheckoutResponse(
        UUID bookingReference,
        Long eventId,
        Integer quantity,
        BigDecimal totalAmount,
        BookingStatus status,
        Instant expiresAt,
        List<String> seatNumbers
) {
    public static CheckoutResponse from(
            Booking booking,
            List<String> seatNumbers
    ) {
        return new CheckoutResponse(
                booking.getBookingReference(),
                booking.getEvent().getId(),
                booking.getQuantity(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getExpiresAt(),
                List.copyOf(seatNumbers)
        );
    }
}