package com.festora.billing.refund;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "billing_refunds",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_billing_refund_booking",
            columnNames = "booking_reference"
        )
    }
)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true)
    private UUID bookingReference;

    @Column(name = "refund_reference", nullable = false, unique = true)
    private String refundReference;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Refund() {
    }

    public static Refund successful(
            UUID bookingReference,
            BigDecimal amount,
            String reason
    ) {
        Refund refund = new Refund();
        refund.bookingReference = bookingReference;
        refund.refundReference = "FREF-" + UUID.randomUUID();
        refund.amount = amount;
        refund.reason = reason;
        refund.status = RefundStatus.SUCCEEDED;
        refund.createdAt = Instant.now();
        return refund;
    }

    public Long getId() {
        return id;
    }

    public UUID getBookingReference() {
        return bookingReference;
    }

    public String getRefundReference() {
        return refundReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}