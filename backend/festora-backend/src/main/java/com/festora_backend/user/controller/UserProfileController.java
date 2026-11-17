
package com.festora_backend.user.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.user.dto.UserProfileResponse;
import com.festora_backend.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            Authentication authentication
    ) {
        UserProfileResponse profile = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(UserProfileResponse::from)
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found")
                );

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", profile)
        );
    }
}
