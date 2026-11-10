package com.festora_backend.event.repository;
import com.festora_backend.event.entity.Event;
import com.festora_backend.common.enums.EventStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Collection;
import java.util.Optional;
import java.util.List;
public interface EventRepository extends
        JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {
    List<Event> findByOrganizerId(Long organizerId);
    long countByStatus(EventStatus status);
    boolean existsByVenueIdAndStatus(
            Long venueId,
            EventStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e FROM Event e
            WHERE e.id = :eventId
            """)
    Optional<Event> findByIdForUpdate(
            @Param("eventId") Long eventId);
    @Query("""
            SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END
            FROM Event e
            WHERE e.venue.id = :venueId
              AND e.status IN :statuses
              AND (:excludeEventId IS NULL OR e.id <> :excludeEventId)
              AND e.startAt < :requestedEnd
              AND e.endAt > :requestedStart
            """)
    boolean hasVenueConflict(
            @Param("venueId") Long venueId,
            @Param("requestedStart") Instant requestedStart,
            @Param("requestedEnd") Instant requestedEnd,
            @Param("excludeEventId") Long excludeEventId,
            @Param("statuses") Collection<EventStatus> statuses);
}