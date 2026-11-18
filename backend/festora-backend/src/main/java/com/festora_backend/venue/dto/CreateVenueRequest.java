package com.festora_backend.venue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVenueRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 500)
        String address,

        @NotNull 
        @Min(1)
        Integer capacity
) {
}