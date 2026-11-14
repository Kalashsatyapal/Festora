package com.festora_backend.auth.service;

import com.festora_backend.auth.dto.*;
import com.festora_backend.auth.security.JwtService;
import com.festora_backend.common.enums.*;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public RegisterResponse register(
            RegisterRequest request,
            UserRole role
    ) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already registered");
        }

        if (role == UserRole.ADMIN) {
            throw new BusinessException(
                    "Public admin registration is not permitted"
            );
        }

        User user = User.create(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                role
        );

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getStatus(),
                role == UserRole.ORGANIZER
                        ? "Organizer registration submitted for approval"
                        : "Registration successful"
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email().trim().toLowerCase(),
                                request.password()
                        )
                );

        User user = userRepository.findByEmailIgnoreCase(
                authentication.getName()
        ).orElseThrow(() ->
                new BusinessException("Invalid credentials"));

        if (!user.isEnabled()
                || user.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active");
        }

        RefreshTokenResult refresh = refreshTokenService.create(user);

        return buildAuthResponse(user, refresh);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenResult refresh =
                refreshTokenService.rotate(rawRefreshToken);

        return buildAuthResponse(
                refresh.user(),
                refresh
        );
    }

    private AuthResponse buildAuthResponse(
            User user,
            RefreshTokenResult refresh
    ) {
        Instant now = Instant.now();

        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                now.plusMillis(jwtService.getExpirationMillis()),
                refresh.refreshToken(),
                refresh.refreshExpiresAt(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}