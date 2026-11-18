package com.festora_backend.venue.dto;

public record AdminVenueResponse(
        Long id,
        String name,
        String city,
        String address,
        Integer capacity,
        boolean active
) {
}