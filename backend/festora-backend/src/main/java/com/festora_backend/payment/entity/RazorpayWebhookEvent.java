package com.festora_backend.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "razorpay_webhook_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_razorpay_webhook_event_id",
                columnNames = "event_id"
        )
)
public class RazorpayWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    protected RazorpayWebhookEvent() {
    }

    public static RazorpayWebhookEvent create(
            String eventId,
            String eventType
    ) {
        RazorpayWebhookEvent event = new RazorpayWebhookEvent();
        event.eventId = eventId;
        event.eventType = eventType;
        event.receivedAt = Instant.now();
        return event;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}