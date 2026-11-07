package com.festora_backend.user.controller;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import com.festora_backend.user.service.AvatarStorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
public class AvatarController {

    private final AvatarStorageService storageService;
    private final UserRepository userRepository;

    public AvatarController(
            AvatarStorageService storageService,
            UserRepository userRepository
    ) {
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    @PostMapping(
            value = "/avatar",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        User user = userRepository.findByEmailIgnoreCase(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException("Authenticated user not found"));

        String avatarUrl = storageService.store(file);

        user.updateAvatar(avatarUrl);
        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Avatar uploaded successfully",
                        avatarUrl
                )
        );
    }
}