package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.BillingRefundRequest;
import com.festora_backend.admin.dto.BillingRefundResponse;
import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.payment.entity.PaymentRecord;
import com.festora_backend.payment.repository.PaymentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminRefundService {

    private final BookingRepository bookingRepository;
    private final RefundAuditRepository refundRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final BillingClient billingClient;

    public AdminRefundService(
            BookingRepository bookingRepository,
            RefundAuditRepository refundRepository,
            PaymentRecordRepository paymentRecordRepository,
            BillingClient billingClient
    ) {
        this.bookingRepository = bookingRepository;
        this.refundRepository = refundRepository;
        this.paymentRecordRepository = paymentRecordRepository;
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
                        new ResourceNotFoundException("Booking", bookingReference)
                );

        if (booking.getStatus() != BookingStatus.CANCELLED
                && booking.getStatus() != BookingStatus.REFUND_PENDING) {
            throw new BusinessException(
                    "Only cancelled bookings can be refunded"
            );
        }

        RefundAudit audit = refundRepository
                .findByBookingId(booking.getId())
                .orElse(null);

        if (audit != null
                && audit.getStatus() != PaymentStatus.FAILED
                && audit.getStatus() != PaymentStatus.REFUND_PENDING) {
            throw new BusinessException(
                    "A refund has already been completed for this booking"
            );
        }

        PaymentRecord payment = paymentRecordRepository
                .findByBooking_Id(booking.getId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Payment record was not found for this booking"
                        )
                );

        if (payment.getStatus() != PaymentStatus.SUCCESS
                || payment.getRazorpayPaymentId() == null
                || payment.getRazorpayPaymentId().isBlank()) {
            throw new BusinessException(
                    "A successful Razorpay payment is required for refund"
            );
        }

        if (audit == null) {
            audit = refundRepository.save(
                    RefundAudit.create(booking, reason.trim())
            );
        } else {
            audit.markPending();
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
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
        }

        BillingRefundResponse billingResponse;

        try {
            billingResponse = billingClient.refund(
                    new BillingRefundRequest(
                            booking.getBookingReference(),
                            payment.getRazorpayPaymentId(),
                            audit.getAmount(),
                            audit.getReason()
                    )
            );
        } catch (RuntimeException exception) {
            audit.markPending();
            return toResponse(audit);
        }

        if (billingResponse == null) {
            audit.markPending();
            return toResponse(audit);
        }

        if ("SUCCEEDED".equalsIgnoreCase(billingResponse.status())) {
            audit.markRefunded(billingResponse.refundReference());

            int completed = bookingRepository.updateBookingStatus(
                    booking.getId(),
                    BookingStatus.REFUND_PENDING,
                    BookingStatus.REFUNDED
            );

            if (completed != 1) {
                throw new BusinessException(
                        "Unable to complete booking refund status"
                );
            }
        } else if ("FAILED".equalsIgnoreCase(billingResponse.status())) {
            audit.markFailed(billingResponse.message());

            bookingRepository.updateBookingStatus(
                    booking.getId(),
                    BookingStatus.REFUND_PENDING,
                    BookingStatus.CANCELLED
            );
        } else {
            audit.markPending();
        }

        return toResponse(audit);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefund(Long refundId) {
        RefundAudit audit = refundRepository.findById(refundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refund", refundId)
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