package com.festora_backend.auth.dto;

import com.festora_backend.auth.entity.RefreshToken;
import com.festora_backend.auth.repository.RefreshTokenRepository;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.user.entity.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long expirationDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${festora.jwt.refresh-expiration-days:30}")
            long expirationDays
    ) {
        this.repository = repository;
        this.expirationDays = expirationDays;
    }

    @Transactional
    public RefreshTokenResult create(User user) {
        String rawToken = generateToken();
        Instant expiresAt = Instant.now().plusSeconds(
                expirationDays * 86400
        );

        repository.save(
                new RefreshToken(
                        hashToken(rawToken),
                        user,
                        expiresAt
                )
        );

        return new RefreshTokenResult(
                user,
                rawToken,
                expiresAt
        );
    }

    @Transactional
    public RefreshTokenResult rotate(String rawToken) {
        RefreshToken current = repository
                .findByTokenHashForUpdate(hashToken(rawToken))
                .orElseThrow(() ->
                        new BusinessException(
                                "Invalid or expired refresh token"
                        )
                );

        Instant now = Instant.now();

        if (!current.isValid(now)) {
            throw new BusinessException(
                    "Invalid or expired refresh token"
            );
        }

        User user = current.getUser();

        if (!user.isEnabled()
                || user.getStatus() !=
                com.festora_backend.common.enums.AccountStatus.ACTIVE) {
            current.revoke();
            repository.save(current);

            throw new BusinessException(
                    "Account is not active"
            );
        }

        current.revoke();
        repository.save(current);

        String newRawToken = generateToken();
        Instant newExpiry = now.plusSeconds(
                expirationDays * 86400
        );

        repository.save(
                new RefreshToken(
                        hashToken(newRawToken),
                        user,
                        newExpiry
                )
        );

        return new RefreshTokenResult(
                user,
                newRawToken,
                newExpiry
        );
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}