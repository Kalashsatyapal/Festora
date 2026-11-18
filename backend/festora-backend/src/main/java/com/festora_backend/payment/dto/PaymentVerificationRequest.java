package com.festora_backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentVerificationRequest(
        @NotBlank
        @Size(max = 100)
        String razorpayPaymentId,

        @NotBlank
        @Pattern(regexp = "^[a-fA-F0-9]{64}$")
        String razorpaySignature
) {
}