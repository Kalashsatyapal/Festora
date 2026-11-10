
package com.festora_backend.organizer.entity;

import com.festora_backend.user.entity.User;
import com.festora_backend.venue.entity.Venue;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "organizer_venues", uniqueConstraints = @UniqueConstraint(name = "uk_organizer_venue", columnNames = {
        "organizer_id", "venue_id" }), indexes = {
                @Index(name = "idx_organizer_venue_organizer", columnList = "organizer_id"),
                @Index(name = "idx_organizer_venue_venue", columnList = "venue_id")
        })
public class OrganizerVenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt;

    protected OrganizerVenue() {
    }

    public OrganizerVenue(User organizer, Venue venue) {
        this.organizer = organizer;
        this.venue = venue;
        this.active = true;
        this.assignedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public User getOrganizer() {
        return organizer;
    }

    public Venue getVenue() {
        return venue;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
