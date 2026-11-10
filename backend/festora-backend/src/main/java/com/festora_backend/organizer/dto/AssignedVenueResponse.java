package com.festora_backend.organizer.dto;
import java.time.Instant;

public record AssignedVenueResponse(
        Long assignmentId,
        Long venueId,
        String venueName,
        String city,
        String address,
        Integer capacity,
        Instant assignedAt
) {
}