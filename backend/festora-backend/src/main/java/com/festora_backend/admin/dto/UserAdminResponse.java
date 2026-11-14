package com.festora_backend.admin.dto;

import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.user.entity.User;

import java.time.Instant;

public record UserAdminResponse(
        Long id,
        String fullName,
        String email,
        String avatarUrl,
        UserRole role,
        AccountStatus status,
        boolean enabled,
        Instant createdAt
) {
    public static UserAdminResponse from(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getStatus(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}