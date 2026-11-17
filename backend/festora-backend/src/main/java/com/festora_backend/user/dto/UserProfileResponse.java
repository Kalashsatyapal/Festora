
package com.festora_backend.user.dto;

import com.festora_backend.user.entity.User;
import java.time.Instant;

public record UserProfileResponse(
        Long userId,
        String fullName,
        String email,
        String avatarUrl,
        String role,
        String status,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
