package com.festora_backend.organizer.dto;
import java.math.BigDecimal;
public record OrganizerRevenueResponse(
        Long eventId,
        String eventTitle,
        BigDecimal totalRevenue,
        Long confirmedBookings,
        Long ticketsSold,
        String currency
) {
}