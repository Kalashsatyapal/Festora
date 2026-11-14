package com.festora_backend.auth.service;

import com.festora_backend.auth.entity.PasswordResetToken;
import com.festora_backend.auth.repository.PasswordResetTokenRepository;
import com.festora_backend.auth.dto.ForgotPasswordRequest;
import com.festora_backend.auth.dto.ResetPasswordRequest;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;
    private final long expirationMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetEmailService emailService,
            @Value("${festora.password-reset.expiration-minutes:15}")
            long expirationMinutes
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(
                request.email().trim()
        ).ifPresent(user -> {
            if (!user.isEnabled()) {
                return;
            }

            tokenRepository.deleteByUserId(user.getId());

            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);

            String rawToken = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            PasswordResetToken resetToken = new PasswordResetToken(
                    hashToken(rawToken),
                    user,
                    Instant.now().plusSeconds(expirationMinutes * 60)
            );

            tokenRepository.save(resetToken);

            emailService.sendResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    rawToken
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.token());

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new BusinessException(
                                "Invalid or expired password reset token"
                        )
                );

        if (!resetToken.isValid(Instant.now())) {
            throw new BusinessException(
                    "Invalid or expired password reset token"
            );
        }

        User user = resetToken.getUser();

        user.updatePassword(
                passwordEncoder.encode(request.newPassword())
        );

        resetToken.markUsed();

        userRepository.save(user);
        tokenRepository.save(resetToken);
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