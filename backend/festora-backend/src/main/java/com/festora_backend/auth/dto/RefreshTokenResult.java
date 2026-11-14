package com.festora_backend.auth.dto;

import com.festora_backend.user.entity.User;

import java.time.Instant;

public record RefreshTokenResult(
        User user,
        String refreshToken,
        Instant refreshExpiresAt
) {
}