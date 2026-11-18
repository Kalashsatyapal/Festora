package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.BillingRefundRequest;
import com.festora_backend.admin.dto.BillingRefundResponse;
import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminRefundServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RefundAuditRepository refundRepository;

    @Mock
    private BillingClient billingClient;

    @Mock
    private Booking booking;

    @Mock
    private RefundAudit audit;

    @InjectMocks
    private AdminRefundService refundService;

    private final UUID bookingReference = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(bookingRepository.findByBookingReferenceForUpdate(bookingReference))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus()).thenReturn(BookingStatus.CANCELLED);
        when(booking.getId()).thenReturn(10L);
        when(booking.getBookingReference()).thenReturn(bookingReference);
        when(booking.getTotalAmount()).thenReturn(new BigDecimal("1200.00"));

        when(audit.getBooking()).thenReturn(booking);
        when(audit.getAmount()).thenReturn(new BigDecimal("1200.00"));
        when(audit.getReason()).thenReturn("Customer cancellation");
        when(audit.getCreatedAt()).thenReturn(Instant.now());
        when(audit.getUpdatedAt()).thenReturn(Instant.now());

        when(bookingRepository.updateBookingStatus(
                10L,
                BookingStatus.CANCELLED,
                BookingStatus.REFUND_PENDING
        )).thenReturn(1);
    }

    @Test
    void refundShouldCompleteSuccessfully() {
        when(refundRepository.findByBookingId(10L))
                .thenReturn(Optional.empty());

        when(refundRepository.save(any(RefundAudit.class)))
                .thenReturn(audit);

        when(billingClient.refund(any(BillingRefundRequest.class)))
                .thenReturn(new BillingRefundResponse(
                        true,
                        "REF-123",
                        "Refund successful"
                ));

        when(bookingRepository.updateBookingStatus(
                10L,
                BookingStatus.REFUND_PENDING,
                BookingStatus.REFUNDED
        )).thenReturn(1);

        when(audit.getStatus()).thenReturn(PaymentStatus.REFUNDED);
        when(audit.getBillingReference()).thenReturn("REF-123");

        var response = refundService.initiateRefund(
                bookingReference,
                "Customer cancellation"
        );

        assertEquals("REFUNDED", response.status());
        assertEquals("REF-123", response.billingReference());

        verify(audit).markRefunded("REF-123");
    }

    @Test
    void failedRefundShouldBeMarkedFailedAndAllowRetry() {
        when(refundRepository.findByBookingId(10L))
                .thenReturn(Optional.empty());

        when(refundRepository.save(any(RefundAudit.class)))
                .thenReturn(audit);

        when(billingClient.refund(any(BillingRefundRequest.class)))
                .thenThrow(new RuntimeException("Billing unavailable"));

        when(bookingRepository.updateBookingStatus(
                10L,
                BookingStatus.REFUND_PENDING,
                BookingStatus.CANCELLED
        )).thenReturn(1);

        when(audit.getStatus()).thenReturn(PaymentStatus.FAILED);

        var response = refundService.initiateRefund(
                bookingReference,
                "Customer cancellation"
        );

        assertEquals("FAILED", response.status());

        verify(audit).markFailed("Billing unavailable");
    }

    @Test
    void failedRefundShouldBeRetryable() {
        when(refundRepository.findByBookingId(10L))
                .thenReturn(Optional.of(audit));

        when(audit.getStatus()).thenReturn(
                PaymentStatus.FAILED,
                PaymentStatus.REFUNDED
        );

        when(billingClient.refund(any(BillingRefundRequest.class)))
                .thenReturn(new BillingRefundResponse(
                        true,
                        "REF-123",
                        "Refund successful"
                ));

        when(bookingRepository.updateBookingStatus(
                10L,
                BookingStatus.REFUND_PENDING,
                BookingStatus.REFUNDED
        )).thenReturn(1);

        when(audit.getBillingReference()).thenReturn("REF-123");

        var response = refundService.initiateRefund(
                bookingReference,
                "Customer cancellation"
        );

        assertEquals("REFUNDED", response.status());

        verify(audit).markPending();
        verify(audit).markRefunded("REF-123");
        verify(refundRepository, never()).save(any(RefundAudit.class));
    }

    @Test
    void refundShouldRejectConfirmedBookings() {
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

        assertThrows(
                BusinessException.class,
                () -> refundService.initiateRefund(
                        bookingReference,
                        "Customer cancellation"
                )
        );

        verifyNoInteractions(billingClient);
        verify(refundRepository, never()).save(any(RefundAudit.class));
    }

    @Test
    void refundShouldRejectExistingPendingRefund() {
        when(refundRepository.findByBookingId(10L))
                .thenReturn(Optional.of(audit));

        when(audit.getStatus()).thenReturn(PaymentStatus.REFUND_PENDING);

        assertThrows(
                BusinessException.class,
                () -> refundService.initiateRefund(
                        bookingReference,
                        "Customer cancellation"
                )
        );

        verifyNoInteractions(billingClient);
        verify(audit, never()).markPending();
    }
}