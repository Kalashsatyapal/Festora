package com.festora_backend.booking.entity;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.event.entity.Event;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_booking_user", columnList = "user_id"),
        @Index(name = "idx_booking_event", columnList = "event_id"),
        @Index(name = "idx_booking_status_expiry",
                columnList = "status,expires_at")
    }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private BookingStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Booking() {
    }

    public static Booking create(
            User user,
            Event event,
            int quantity,
            BigDecimal totalAmount,
            Instant expiresAt
    ) {
        if (quantity <= 0 || totalAmount == null
                || totalAmount.signum() < 0
                || expiresAt == null
                || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "Invalid booking details"
            );
        }

        Booking booking = new Booking();
        booking.bookingReference = UUID.randomUUID();
        booking.user = user;
        booking.event = event;
        booking.quantity = quantity;
        booking.totalAmount = totalAmount;
        booking.expiresAt = expiresAt;
        booking.status = BookingStatus.PENDING;

        return booking;
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

    public void confirm() {
        if (status != BookingStatus.PENDING
                || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalStateException(
                    "Booking cannot be confirmed"
            );
        }

        status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (status != BookingStatus.CONFIRMED
                && status != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking cannot be cancelled"
            );
        }

        status = BookingStatus.CANCELLED;
    }

    public void expire() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending bookings can expire"
            );
        }

        status = BookingStatus.EXPIRED;
    }

    public Long getId() { return id; }
    public UUID getBookingReference() { return bookingReference; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }
    public List<Ticket> getTickets() { return List.copyOf(tickets); }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}