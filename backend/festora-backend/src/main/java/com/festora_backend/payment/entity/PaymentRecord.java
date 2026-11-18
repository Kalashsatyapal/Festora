package com.festora_backend.payment.entity;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "payment_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_booking",
                        columnNames = "booking_id"
                ),
                @UniqueConstraint(
                        name = "uk_payment_razorpay_order",
                        columnNames = "razorpay_order_id"
                ),
                @UniqueConstraint(
                        name = "uk_payment_razorpay_payment",
                        columnNames = "razorpay_payment_id"
                )
        }
)
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "razorpay_order_id", nullable = false, unique = true, length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true, length = 100)
    private String razorpayPaymentId;

    @Column(nullable = false)
    private Long amountPaise;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PaymentRecord() {
    }

    public static PaymentRecord create(
            Booking booking,
            String razorpayOrderId,
            Long amountPaise,
            String currency
    ) {
        PaymentRecord payment = new PaymentRecord();
        payment.booking = booking;
        payment.razorpayOrderId = razorpayOrderId;
        payment.amountPaise = amountPaise;
        payment.currency = currency;
        payment.status = PaymentStatus.INITIATED;
        return payment;
    }

    public void markSuccessful(String razorpayPaymentId) {
        if (status == PaymentStatus.SUCCESS
                && !razorpayPaymentId.equals(this.razorpayPaymentId)) {
            throw new IllegalStateException(
                    "Payment is already linked to another payment reference"
            );
        }

        this.razorpayPaymentId = razorpayPaymentId;
        this.status = PaymentStatus.SUCCESS;
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

    public Booking getBooking() {
        return booking;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public Long getAmountPaise() {
        return amountPaise;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}