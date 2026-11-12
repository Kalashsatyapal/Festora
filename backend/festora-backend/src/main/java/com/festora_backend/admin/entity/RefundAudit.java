package com.festora_backend.admin.entity;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "refund_audits",
        indexes = {
                @Index(name = "idx_refund_booking", columnList = "booking_id"),
                @Index(name = "idx_refund_status", columnList = "status")
        }
)
public class RefundAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(length = 150)
    private String billingReference;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected RefundAudit() {
    }

    public static RefundAudit create(
            Booking booking,
            String reason
    ) {
        RefundAudit audit = new RefundAudit();
        audit.booking = booking;
        audit.amount = booking.getTotalAmount();
        audit.reason = reason;
        audit.status = PaymentStatus.REFUND_PENDING;
        return audit;
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

    public void markRefunded(String billingReference) {
        this.status = PaymentStatus.REFUNDED;
        this.billingReference = billingReference;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getBillingReference() {
        return billingReference;
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