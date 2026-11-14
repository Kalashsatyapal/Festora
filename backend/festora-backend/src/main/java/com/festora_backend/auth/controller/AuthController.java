package com.festora_backend.auth.controller;

import com.festora_backend.auth.dto.*;
import com.festora_backend.auth.service.AuthService;
import com.festora_backend.auth.service.PasswordResetService;
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
        private final PasswordResetService passwordResetService;

        public AuthController(
                        AuthService authService,
                        PasswordResetService passwordResetService) {
                this.authService = authService;
                this.passwordResetService = passwordResetService;
        }

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.success(
                                                "User registered successfully",
                                                authService.register(request, UserRole.USER)));
        }

        @PostMapping("/register/organizer")
        public ResponseEntity<ApiResponse<RegisterResponse>> registerOrganizer(
                        @Valid @RequestBody RegisterRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.success(
                                                "Organizer registration submitted",
                                                authService.register(request, UserRole.ORGANIZER)));
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse<Void>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request) {
                passwordResetService.forgotPassword(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "If the account exists, password reset instructions will be sent",
                                                null));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {
                passwordResetService.resetPassword(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Password reset successfully",
                                                null));
        }

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Login successful",
                                                authService.login(request)));
        }

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<AuthResponse>> refresh(
                        @Valid @RequestBody RefreshRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Token refreshed successfully",
                                                authService.refresh(request.refreshToken())));
        }
}