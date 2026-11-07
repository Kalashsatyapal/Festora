package com.festora_backend.event.repository;

import com.festora_backend.event.entity.Event;
import com.festora_backend.common.enums.EventStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventRepository extends
        JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    List<Event> findByOrganizerId(Long organizerId);

    long countByStatus(EventStatus status);

    boolean existsByVenueIdAndStatus(
            Long venueId,
            EventStatus status
    );
}