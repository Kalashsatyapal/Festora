package com.festora_backend.auth.controller;

import com.festora_backend.auth.dto.*;
import com.festora_backend.auth.service.AuthService;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.common.response.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "User registered successfully",
                        authService.register(request, UserRole.USER)
                )
        );
    }

    @PostMapping("/register/organizer")
    public ResponseEntity<ApiResponse<RegisterResponse>> registerOrganizer(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Organizer registration submitted",
                        authService.register(request, UserRole.ORGANIZER)
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(request)
                )
        );
    }
}