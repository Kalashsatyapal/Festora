package com.festora_backend.event.entity;

import com.festora_backend.category.entity.Category;
import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.user.entity.User;
import com.festora_backend.venue.entity.Venue;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_event_category", columnList = "category_id"),
        @Index(name = "idx_event_venue", columnList = "venue_id"),
        @Index(name = "idx_event_dates_status",
                columnList = "start_at,end_at,status"),
        @Index(name = "idx_event_organizer",
                columnList = "organizer_id")
    }
)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 3000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(nullable = false)
    private Integer totalCapacity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Event() {
    }

    public static Event create(
            String title,
            String description,
            Category category,
            Venue venue,
            Instant startAt,
            Instant endAt,
            Integer totalCapacity,
            BigDecimal ticketPrice,
            User organizer
    ) {
        Event event = new Event();
        event.updateDetails(
                title,
                description,
                category,
                venue,
                startAt,
                endAt,
                totalCapacity,
                ticketPrice
        );
        event.organizer = organizer;
        event.status = EventStatus.DRAFT;
        return event;
    }

    public void updateDetails(
            String title,
            String description,
            Category category,
            Venue venue,
            Instant startAt,
            Instant endAt,
            Integer totalCapacity,
            BigDecimal ticketPrice
    ) {
        if (title == null || title.isBlank()
                || category == null
                || venue == null
                || startAt == null
                || endAt == null
                || !endAt.isAfter(startAt)
                || totalCapacity == null
                || totalCapacity <= 0
                || ticketPrice == null
                || ticketPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Invalid event details"
            );
        }

        if (totalCapacity > venue.getCapacity()) {
            throw new IllegalArgumentException(
                    "Event capacity exceeds venue capacity"
            );
        }

        this.title = title.trim();
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalCapacity = totalCapacity;
        this.ticketPrice = ticketPrice;
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft events can be published"
            );
        }

        status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (status == EventStatus.COMPLETED
                || status == EventStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Event cannot be cancelled"
            );
        }

        status = EventStatus.CANCELLED;
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

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Venue getVenue() { return venue; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public Integer getTotalCapacity() { return totalCapacity; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public EventStatus getStatus() { return status; }
    public User getOrganizer() { return organizer; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}