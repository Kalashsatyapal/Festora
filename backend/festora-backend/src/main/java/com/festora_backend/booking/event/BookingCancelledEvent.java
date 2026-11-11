package com.festora_backend.booking.event;

import java.util.UUID;

public record BookingCancelledEvent(
        UUID bookingReference,
        Long userId,
        Long eventId
) {
}