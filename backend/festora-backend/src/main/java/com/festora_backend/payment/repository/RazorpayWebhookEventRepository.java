package com.festora_backend.payment.repository;

import com.festora_backend.payment.entity.RazorpayWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RazorpayWebhookEventRepository
        extends JpaRepository<RazorpayWebhookEvent, Long> {

    boolean existsByEventId(String eventId);
}