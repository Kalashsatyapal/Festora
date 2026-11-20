package com.festora_backend.admin.service;
import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.common.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundRecoverySchedulerTest {

    @Mock
    private RefundAuditRepository refundRepository;

    @Mock
    private AdminRefundService adminRefundService;

    @InjectMocks
    private RefundRecoveryScheduler scheduler;

    @Test
    void shouldRetryAllPendingRefunds() {
        RefundAudit firstAudit = mock(RefundAudit.class);
        RefundAudit secondAudit = mock(RefundAudit.class);

        Booking firstBooking = mock(Booking.class);
        Booking secondBooking = mock(Booking.class);

        UUID firstReference = UUID.randomUUID();
        UUID secondReference = UUID.randomUUID();

        when(firstAudit.getBooking()).thenReturn(firstBooking);
        when(secondAudit.getBooking()).thenReturn(secondBooking);

        when(firstBooking.getBookingReference()).thenReturn(firstReference);
        when(secondBooking.getBookingReference()).thenReturn(secondReference);

        when(firstAudit.getReason()).thenReturn("First refund");
        when(secondAudit.getReason()).thenReturn("Second refund");

        when(refundRepository.findByStatusWithBooking(
                PaymentStatus.REFUND_PENDING
        )).thenReturn(List.of(firstAudit, secondAudit));

        scheduler.retryPendingRefunds();

        verify(adminRefundService).initiateRefund(
                firstReference,
                "First refund"
        );

        verify(adminRefundService).initiateRefund(
                secondReference,
                "Second refund"
        );
    }

    @Test
    void shouldContinueWhenOneRefundAttemptThrowsException() {
        RefundAudit firstAudit = mock(RefundAudit.class);
        RefundAudit secondAudit = mock(RefundAudit.class);

        Booking firstBooking = mock(Booking.class);
        Booking secondBooking = mock(Booking.class);

        UUID firstReference = UUID.randomUUID();
        UUID secondReference = UUID.randomUUID();

        when(firstAudit.getBooking()).thenReturn(firstBooking);
        when(secondAudit.getBooking()).thenReturn(secondBooking);

        when(firstBooking.getBookingReference()).thenReturn(firstReference);
        when(secondBooking.getBookingReference()).thenReturn(secondReference);

        when(firstAudit.getReason()).thenReturn("First refund");
        when(secondAudit.getReason()).thenReturn("Second refund");

        when(refundRepository.findByStatusWithBooking(
                PaymentStatus.REFUND_PENDING
        )).thenReturn(List.of(firstAudit, secondAudit));

        doThrow(new RuntimeException("Billing unavailable"))
                .when(adminRefundService)
                .initiateRefund(firstReference, "First refund");

        scheduler.retryPendingRefunds();

        verify(adminRefundService).initiateRefund(
                firstReference,
                "First refund"
        );

        verify(adminRefundService).initiateRefund(
                secondReference,
                "Second refund"
        );
    }

    @Test
    void shouldNotCallRefundServiceWhenNoPendingRefundsExist() {
        when(refundRepository.findByStatusWithBooking(
                PaymentStatus.REFUND_PENDING
        )).thenReturn(List.of());

        scheduler.retryPendingRefunds();

        verifyNoInteractions(adminRefundService);
    }
}
