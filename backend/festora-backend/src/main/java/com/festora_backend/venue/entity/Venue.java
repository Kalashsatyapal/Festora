package com.festora_backend.venue.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "venues",
    indexes = {
        @Index(name = "idx_venue_city", columnList = "city"),
        @Index(name = "idx_venue_active", columnList = "active")
    }
)
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Venue() {
    }

    public Venue(String name, String city, String address, Integer capacity) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
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
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public Integer getCapacity() { return capacity; }
    public boolean isActive() { return active; }
}