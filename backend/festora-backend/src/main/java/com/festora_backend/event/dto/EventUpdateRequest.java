package com.festora_backend.event.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record EventUpdateRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 3000)
        String description,

        @NotNull
        Long categoryId,

        @NotNull
        Long venueId,

        @NotNull
        Instant startAt,

        @NotNull
        Instant endAt,

        @NotNull
        @Positive
        Integer totalCapacity,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal ticketPrice
) {
}