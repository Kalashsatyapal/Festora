package com.festora_backend.event.dto;

import com.festora_backend.common.enums.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        Long categoryId,
        String categoryName,
        Long venueId,
        String venueName,
        String venueCity,
        String venueAddress,
        Instant startAt,
        Instant endAt,
        Integer totalCapacity,
        BigDecimal ticketPrice,
        EventStatus status,
        Long organizerId,
        String organizerName,
        Instant createdAt
) {
}