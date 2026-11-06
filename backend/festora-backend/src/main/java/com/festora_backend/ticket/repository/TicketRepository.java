package com.festora_backend.ticket.repository;

import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.common.enums.TicketStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventIdAndStatus(
            Long eventId,
            TicketStatus status
    );

    long countByEventIdAndStatus(
            Long eventId,
            TicketStatus status
    );

    List<Ticket> findByBookingId(Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM Ticket t
        WHERE t.id = :ticketId
        """)
    Optional<Ticket> findByIdForUpdate(
            @Param("ticketId") Long ticketId
    );

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.status = :status
        AND t.reservedUntil <= :now
        """)
    List<Ticket> findExpiredReservations(
            @Param("status") TicketStatus status,
            @Param("now") Instant now
    );
}