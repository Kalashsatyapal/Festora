
package com.festora_backend.payment.repository;

import com.festora_backend.payment.entity.PaymentRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecordRepository
        extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByBooking_Id(Long bookingId);

    Optional<PaymentRecord> findByRazorpayOrderId(String razorpayOrderId);
}
