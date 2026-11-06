package com.festora_backend.booking.repository;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(
            java.util.UUID bookingReference
    );

    List<Booking> findByUserId(Long userId);

    List<Booking> findByEventId(Long eventId);

    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status,
            Instant expiryTime
    );

    long countByStatus(BookingStatus status);
}