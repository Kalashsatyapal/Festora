package com.festora_backend.admin.repository;

import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.common.enums.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface RefundAuditRepository
        extends JpaRepository<RefundAudit, Long> {

    Optional<RefundAudit> findByBookingId(Long bookingId);

    @Query(
            value = """
                    SELECT r
                    FROM RefundAudit r
                    JOIN r.booking b
                    JOIN b.user u
                    JOIN b.event e
                    WHERE (:status IS NULL OR r.status = :status)
                    AND (
                        :search IS NULL
                        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(r.billingReference) LIKE LOWER(CONCAT('%', :search, '%'))
                    )
                    """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM RefundAudit r
                    JOIN r.booking b
                    JOIN b.user u
                    JOIN b.event e
                    WHERE (:status IS NULL OR r.status = :status)
                    AND (
                        :search IS NULL
                        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(r.billingReference) LIKE LOWER(CONCAT('%', :search, '%'))
                    )
                    """
    )
    Page<RefundAudit> searchAdminRefunds(
            @Param("status") PaymentStatus status,
            @Param("search") String search,
            Pageable pageable
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