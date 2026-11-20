
package com.festora_backend.admin.dto;

public record BillingRefundResponse(
        boolean successful,
        String refundReference,
        String status,
        String message
) {
    public BillingRefundResponse(
            boolean successful,
            String refundReference,
            String message
    ) {
        this(
                successful,
                refundReference,
                successful ? "SUCCEEDED" : "FAILED",
                message
        );
    }
}
