package com.festora_backend.admin.dto;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingAdminResponse(
        Long id,
        UUID bookingReference,
        Long userId,
        String customerName,
        String customerEmail,
        Long eventId,
        String eventTitle,
        Integer quantity,
        BigDecimal totalAmount,
        BookingStatus status,
        Instant expiresAt,
        Instant createdAt
) {
    public static BookingAdminResponse from(Booking booking) {
        return new BookingAdminResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getUser().getId(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                booking.getEvent().getId(),
                booking.getEvent().getTitle(),
                booking.getQuantity(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getExpiresAt(),
                booking.getCreatedAt()
        );
    }
}