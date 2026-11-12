package com.festora_backend.admin.dto;

import java.time.Instant;

public record OrganizerVerificationResponse(
        Long organizerId,
        String fullName,
        String email,
        String status,
        boolean enabled,
        Instant createdAt
) {
}