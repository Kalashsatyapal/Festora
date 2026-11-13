package com.festora.billing.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByBookingReference(UUID bookingReference);
}