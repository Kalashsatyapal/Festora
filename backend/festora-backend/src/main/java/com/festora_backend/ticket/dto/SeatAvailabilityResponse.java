
package com.festora_backend.ticket.dto;

import com.festora_backend.common.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SeatAvailabilityResponse(
        String seatNumber,
        BigDecimal price,
        TicketStatus status,
        Instant reservedUntil
) {
}
