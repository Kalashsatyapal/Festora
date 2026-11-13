package com.festora.billing.refund;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundRequest(
        @NotNull UUID bookingReference,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(max = 500) String reason
) {
}