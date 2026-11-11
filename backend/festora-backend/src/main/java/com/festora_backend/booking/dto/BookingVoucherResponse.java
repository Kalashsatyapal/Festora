package com.festora_backend.booking.dto;

import java.time.Instant;
import java.util.List;

public record BookingVoucherResponse(
        String title,
        String bookingReferenceLabel,
        String eventIdLabel,
        String ticketNumberLabel,
        String seatNumberLabel,
        String bookingReference,
        Long eventId,
        Instant issuedAt,
        List<TicketVoucherResponse> tickets
) {
    public BookingVoucherResponse {
        tickets = List.copyOf(tickets);
    }
}