package com.festora_backend.ticket.dto;

import java.time.Instant;

public record TicketVerificationResponse(
        String ticketNumber,
        String eventTitle,
        String seatNumber,
        String status,
        Instant usedAt
) {
}