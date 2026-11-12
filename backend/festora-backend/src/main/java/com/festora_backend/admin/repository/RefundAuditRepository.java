package com.festora_backend.admin.repository;

import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.common.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RefundAuditRepository
        extends JpaRepository<RefundAudit, Long> {

    Optional<RefundAudit> findByBookingId(Long bookingId);

    List<RefundAudit> findByStatusOrderByCreatedAtDesc(
            PaymentStatus status
    );

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM RefundAudit r
        WHERE r.status = :status
        """)
    BigDecimal sumAmountByStatus(
            @Param("status") PaymentStatus status
    );
}