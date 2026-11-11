package com.festora_backend.ticket.repository;

import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.common.enums.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.booking.id = :bookingId
                        ORDER BY t.seatNumber
                        """)
        List<Ticket> findByBookingIdForUpdate(
                        @Param("bookingId") Long bookingId);

        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Transactional
        @Query("""
                        UPDATE Ticket t
                        SET t.status = com.festora_backend.common.enums.TicketStatus.AVAILABLE,
                            t.holder = null,
                            t.booking = null,
                            t.reservedUntil = null,
                            t.soldAt = null,
                            t.version = t.version + 1
                        WHERE t.booking.id = :bookingId
                        AND t.status IN :statuses
                        """)
        int releaseTicketsForBooking(
                        @Param("bookingId") Long bookingId,
                        @Param("statuses") List<TicketStatus> statuses);

        List<Ticket> findByEventIdAndStatus(
                        Long eventId,
                        TicketStatus status);

        long countByEventIdAndStatus(
                        Long eventId,
                        TicketStatus status);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.event.id = :eventId
                        AND t.status = :status
                        ORDER BY t.seatNumber
                        """)
        List<Ticket> findAvailableForUpdate(
                        @Param("eventId") Long eventId,
                        @Param("status") TicketStatus status,
                        Pageable pageable);

        List<Ticket> findByBookingId(Long bookingId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.id = :ticketId
                        """)
        Optional<Ticket> findByIdForUpdate(
                        @Param("ticketId") Long ticketId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.status = :status
                        AND t.reservedUntil <= :now
                        """)
        List<Ticket> findExpiredReservations(
                        @Param("status") TicketStatus status,
                        @Param("now") Instant now);
}