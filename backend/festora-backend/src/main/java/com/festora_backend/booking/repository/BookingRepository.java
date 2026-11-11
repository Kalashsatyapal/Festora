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

        List<Booking> findByUserId(Long userId);

        List<Booking> findByEventId(Long eventId);

        List<Booking> findByStatusAndExpiresAtBefore(
                        BookingStatus status,
                        Instant expiryTime);

        long countByStatus(BookingStatus status);
}