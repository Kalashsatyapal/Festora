package com.festora_backend.admin.dto;

public record BillingRefundResponse(
        boolean successful,
        String refundReference,
        String message
) {
}