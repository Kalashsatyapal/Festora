package com.festora_backend.user.entity;

import com.festora_backend.common.enums.EventStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_event_category", columnList = "category"),
        @Index(name = "idx_event_venue", columnList = "venue_name"),
        @Index(name = "idx_event_date_status",
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

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "venue_name", nullable = false, length = 150)
    private String venueName;

    @Column(length = 500)
    private String venueAddress;

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
    @JoinColumn(
        name = "organizer_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_event_organizer")
    )
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
            String category,
            String venueName,
            String venueAddress,
            Instant startAt,
            Instant endAt,
            Integer totalCapacity,
            BigDecimal ticketPrice,
            User organizer
    ) {
        Event event = new Event();
        event.title = title;
        event.description = description;
        event.category = category;
        event.venueName = venueName;
        event.venueAddress = venueAddress;
        event.startAt = startAt;
        event.endAt = endAt;
        event.totalCapacity = totalCapacity;
        event.ticketPrice = ticketPrice;
        event.organizer = organizer;
        event.status = EventStatus.DRAFT;
        return event;
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

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft events can be published"
            );
        }

        status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        status = EventStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public EventStatus getStatus() {
        return status;
    }

    public User getOrganizer() {
        return organizer;
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