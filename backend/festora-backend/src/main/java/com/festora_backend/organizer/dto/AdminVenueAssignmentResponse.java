package com.festora_backend.organizer.dto;

import java.time.Instant;

public record AdminVenueAssignmentResponse(
        Long assignmentId,
        Long organizerId,
        String organizerName,
        String organizerEmail,
        Long venueId,
        Instant assignedAt
) {
}