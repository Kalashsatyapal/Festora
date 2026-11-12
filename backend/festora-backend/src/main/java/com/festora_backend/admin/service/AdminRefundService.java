package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.BillingRefundRequest;
import com.festora_backend.admin.dto.BillingRefundResponse;
import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminRefundService {

    private final BookingRepository bookingRepository;
    private final RefundAuditRepository refundRepository;
    private final BillingClient billingClient;

    public AdminRefundService(
            BookingRepository bookingRepository,
            RefundAuditRepository refundRepository,
            BillingClient billingClient
    ) {
        this.bookingRepository = bookingRepository;
        this.refundRepository = refundRepository;
        this.billingClient = billingClient;
    }

    @Transactional
    public RefundResponse initiateRefund(
            UUID bookingReference,
            String reason
    ) {
        Booking booking = bookingRepository
                .findByBookingReferenceForUpdate(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking",
                                bookingReference
                        )
                );

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new BusinessException(
                    "Only cancelled bookings can be refunded"
            );
        }

        if (refundRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BusinessException(
                    "A refund request already exists for this booking"
            );
        }

        RefundAudit audit = refundRepository.save(
                RefundAudit.create(booking, reason.trim())
        );

        int updated = bookingRepository.updateBookingStatus(
                booking.getId(),
                BookingStatus.CANCELLED,
                BookingStatus.REFUND_PENDING
        );

        if (updated != 1) {
            throw new BusinessException(
                    "Unable to update booking refund status"
            );
        }

        try {
            BillingRefundResponse billingResponse =
                    billingClient.refund(
                            new BillingRefundRequest(
                                    booking.getBookingReference(),
                                    booking.getTotalAmount(),
                                    reason.trim()
                            )
                    );

            if (billingResponse == null
                    || !billingResponse.successful()) {
                String message = billingResponse == null
                        ? "Billing service returned an empty response"
                        : billingResponse.message();

                audit.markFailed(message);
                bookingRepository.updateBookingStatus(
                        booking.getId(),
                        BookingStatus.REFUND_PENDING,
                        BookingStatus.CANCELLED
                );
            } else {
                audit.markRefunded(billingResponse.refundReference());
                bookingRepository.updateBookingStatus(
                        booking.getId(),
                        BookingStatus.REFUND_PENDING,
                        BookingStatus.REFUNDED
                );
            }
        } catch (RuntimeException exception) {
            audit.markFailed(exception.getMessage());
            bookingRepository.updateBookingStatus(
                    booking.getId(),
                    BookingStatus.REFUND_PENDING,
                    BookingStatus.CANCELLED
            );
        }

        return toResponse(audit);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefund(Long refundId) {
        RefundAudit audit = refundRepository.findById(refundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Refund",
                                refundId
                        )
                );

        return toResponse(audit);
    }

    private RefundResponse toResponse(RefundAudit audit) {
        return new RefundResponse(
                audit.getId(),
                audit.getBooking().getBookingReference(),
                audit.getAmount(),
                audit.getStatus().name(),
                audit.getReason(),
                audit.getBillingReference(),
                audit.getCreatedAt(),
                audit.getUpdatedAt()
        );
    }
}