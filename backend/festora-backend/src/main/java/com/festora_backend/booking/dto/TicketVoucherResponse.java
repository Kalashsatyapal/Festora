package com.festora_backend.booking.dto;

public record TicketVoucherResponse(
        String ticketNumber,
        String seatNumber,
        String status
) {
}