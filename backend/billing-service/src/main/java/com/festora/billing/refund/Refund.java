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

    @Column(name = "razorpay_payment_id", nullable = false, length = 100)
    private String razorpayPaymentId;

    @Column(name = "refund_reference", unique = true, length = 100)
    private String refundReference;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Refund() {
    }

    public static Refund pending(
            UUID bookingReference,
            String razorpayPaymentId,
            BigDecimal amount,
            String reason
    ) {
        Refund refund = new Refund();
        refund.bookingReference = bookingReference;
        refund.razorpayPaymentId = razorpayPaymentId;
        refund.amount = amount;
        refund.reason = reason;
        refund.status = RefundStatus.PENDING;
        return refund;
    }

    public void markSucceeded(String refundReference) {
        this.refundReference = refundReference;
        this.status = RefundStatus.SUCCEEDED;
        this.failureReason = null;
    }

    public void markPending() {
        this.status = RefundStatus.PENDING;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.status = RefundStatus.FAILED;
        this.failureReason = failureReason;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getBookingReference() {
        return bookingReference;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
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

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}