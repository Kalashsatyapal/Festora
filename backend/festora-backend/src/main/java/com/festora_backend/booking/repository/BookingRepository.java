package com.festora_backend.booking.repository;

import java.util.UUID;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(
            java.util.UUID bookingReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b FROM Booking b
            WHERE b.bookingReference = :reference
            """)
    Optional<Booking> findByBookingReferenceForUpdate(
            @Param("reference") UUID reference);

    @Query("""
            SELECT
                COALESCE(SUM(b.totalAmount), 0) AS totalRevenue,
                COUNT(b.id) AS confirmedBookings,
                COALESCE(SUM(b.quantity), 0) AS ticketsSold
            FROM Booking b
            WHERE b.event.id = :eventId
            AND b.event.organizer.id = :organizerId
            AND b.status = :status
            """)
    OrganizerRevenueProjection getOrganizerEventRevenue(
            @Param("eventId") Long eventId,
            @Param("organizerId") Long organizerId,
            @Param("status") BookingStatus status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByEventId(Long eventId);

    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status,
            Instant expiryTime);

    long countByStatus(BookingStatus status);

    @org.springframework.data.jpa.repository.Modifying
@org.springframework.data.jpa.repository.Query("""
    UPDATE Booking b
    SET b.status = :newStatus
    WHERE b.id = :bookingId
    AND b.status = :currentStatus
    """)
int updateBookingStatus(
        @org.springframework.data.repository.query.Param("bookingId") Long bookingId,
        @org.springframework.data.repository.query.Param("currentStatus") BookingStatus currentStatus,
        @org.springframework.data.repository.query.Param("newStatus") BookingStatus newStatus
);
    @Query("""
            SELECT COALESCE(SUM(b.quantity), 0)
            FROM Booking b
            WHERE b.status = :status
            """)
    Long sumQuantityByStatus(
            @Param("status") BookingStatus status);

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount), 0)
            FROM Booking b
            WHERE b.status = :status
            """)
    java.math.BigDecimal sumTotalAmountByStatus(
            @Param("status") BookingStatus status);

}