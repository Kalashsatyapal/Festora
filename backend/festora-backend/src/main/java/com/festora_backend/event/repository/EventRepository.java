package com.festora_backend.event.repository;

import com.festora_backend.event.entity.Event;
import com.festora_backend.common.enums.EventStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends
        JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByCategoryAndStatus(
            String category,
            EventStatus status
    );

    List<Event> findByVenueNameAndStatus(
            String venueName,
            EventStatus status
    );

    List<Event> findByStartAtBetweenAndStatus(
            Instant start,
            Instant end,
            EventStatus status
    );

    long countByStatus(EventStatus status);
}