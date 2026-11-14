package com.festora_backend.auth.dto;

import com.festora_backend.common.enums.UserRole;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String refreshToken,
        Instant refreshExpiresAt,
        Long userId,
        String fullName,
        String email,
        UserRole role
) {
}