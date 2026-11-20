
package com.festora_backend.admin.service;

import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.common.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RefundRecoveryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(RefundRecoveryScheduler.class);

    private final RefundAuditRepository refundRepository;
    private final AdminRefundService adminRefundService;

    public RefundRecoveryScheduler(
            RefundAuditRepository refundRepository,
            AdminRefundService adminRefundService
    ) {
        this.refundRepository = refundRepository;
        this.adminRefundService = adminRefundService;
    }

    @Scheduled(
            fixedDelayString =
                    "${festora.refund.recovery-interval-ms:60000}"
    )
    public void retryPendingRefunds() {
        List<RefundAudit> pendingRefunds =
                refundRepository.findByStatusWithBooking(
                        PaymentStatus.REFUND_PENDING
                );

        for (RefundAudit audit : pendingRefunds) {
            try {
                adminRefundService.initiateRefund(
                        audit.getBooking().getBookingReference(),
                        audit.getReason()
                );
            } catch (RuntimeException exception) {
                log.warn(
                        "Refund recovery failed for refundId={}",
                        audit.getId(),
                        exception
                );
            }
        }
    }
}
