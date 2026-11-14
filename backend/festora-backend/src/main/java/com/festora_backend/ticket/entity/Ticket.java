package com.festora_backend.ticket.entity;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.event.entity.Event;
import com.festora_backend.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_seat", columnNames = { "event_id", "seat_number" })
}, indexes = {
        @Index(name = "idx_ticket_event_status", columnList = "event_id,status"),
        @Index(name = "idx_ticket_reservation_expiry", columnList = "status,reserved_until"),
        @Index(name = "idx_ticket_holder", columnList = "holder_id")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_event"))
    private Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holder_id", foreignKey = @ForeignKey(name = "fk_ticket_holder"))
    private User holder;

    @Column(name = "seat_number", nullable = false, length = 30)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "reserved_until")
    private Instant reservedUntil;

    @Column(name = "sold_at")
    private Instant soldAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Ticket() {
    }

    public static Ticket create(
            Event event,
            String seatNumber,
            BigDecimal price) {
        Ticket ticket = new Ticket();
        ticket.ticketNumber = UUID.randomUUID();
        ticket.event = event;
        ticket.seatNumber = seatNumber;
        ticket.price = price;
        ticket.status = TicketStatus.AVAILABLE;

        return ticket;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void markUsed() {
        if (status != TicketStatus.SOLD) {
            throw new IllegalStateException(
                    "Only sold tickets can be verified");
        }

        status = TicketStatus.USED;
        usedAt = Instant.now();
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

    public void reserve(User user, Instant expiryTime) {
        if (status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Ticket is not available");
        }

        if (user == null || expiryTime == null
                || !expiryTime.isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "Invalid reservation details");
        }

        holder = user;
        status = TicketStatus.RESERVED;
        reservedUntil = expiryTime;
    }

    public void confirmSale() {
        if (status != TicketStatus.RESERVED) {
            throw new IllegalStateException(
                    "Ticket is not reserved");
        }

        if (reservedUntil == null
                || !reservedUntil.isAfter(Instant.now())) {
            throw new IllegalStateException(
                    "Ticket reservation has expired");
        }

        status = TicketStatus.SOLD;
        soldAt = Instant.now();
        reservedUntil = null;
    }

    public void releaseReservation() {
        if (status != TicketStatus.RESERVED) {
            throw new IllegalStateException(
                    "Only reserved tickets can be released");
        }

        status = TicketStatus.AVAILABLE;
        holder = null;
        booking = null;
        reservedUntil = null;
    }

    public void assignBooking(Booking booking) {
        if (status != TicketStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Ticket is not available");
        }

        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }

    public Long getId() {
        return id;
    }

    public UUID getTicketNumber() {
        return ticketNumber;
    }

    public Event getEvent() {
        return event;
    }

    public User getHolder() {
        return holder;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public Instant getSoldAt() {
        return soldAt;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}