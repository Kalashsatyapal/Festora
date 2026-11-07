package com.festora_backend.auth.dto;

import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;

public record RegisterResponse(
        Long userId,
        String fullName,
        String email,
        UserRole role,
        AccountStatus status,
        String message
) {
}