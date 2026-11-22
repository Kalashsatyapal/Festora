package com.festora_backend.booking.service;

import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.admin.service.AdminRefundService;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import com.festora_backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AdminRefundService adminRefundService;

    @Mock
    private Booking booking;

    @Mock
    private User user;

    @Mock
    private Ticket ticket1;

    @Mock
    private Ticket ticket2;

    private BookingCancellationService cancellationService;
    private UUID bookingReference;

    @BeforeEach
    void setUp() {
        cancellationService = new BookingCancellationService(
                bookingRepository,
                ticketRepository,
                adminRefundService
        );

        bookingReference = UUID.randomUUID();

        when(bookingRepository.findByBookingReferenceForUpdate(bookingReference))
                .thenReturn(Optional.of(booking));

        lenient().when(booking.getId()).thenReturn(10L);
        lenient().when(booking.getBookingReference()).thenReturn(bookingReference);
        lenient().when(booking.getQuantity()).thenReturn(2);
        when(booking.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("user@test.com");
    }

    @Test
    void paidBookingShouldBeCancelledAndTicketsReleasedAfterRefund() {
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

        when(ticketRepository.findByBookingIdForUpdate(10L))
                .thenReturn(List.of(ticket1, ticket2));

        when(ticket1.getStatus()).thenReturn(TicketStatus.SOLD);
        when(ticket2.getStatus()).thenReturn(TicketStatus.SOLD);

        when(adminRefundService.initiateRefund(
                bookingReference,
                "Customer cancellation"
        )).thenReturn(new RefundResponse(
                1L,
                bookingReference,
                new BigDecimal("1200.00"),
                "REFUNDED",
                "Customer cancellation",
                "REF-123",
                Instant.now(),
                Instant.now()
        ));

        when(ticketRepository.releaseTicketsForBooking(
                10L,
                List.of(TicketStatus.SOLD)
        )).thenReturn(2);

        RefundResponse response = cancellationService.cancelPaidBooking(
                bookingReference,
                "USER@test.com"
        );

        assertEquals("REFUNDED", response.status());

        verify(booking).cancel();

        verify(ticketRepository).releaseTicketsForBooking(
                10L,
                List.of(TicketStatus.SOLD)
        );
    }

    @Test
    void pendingRefundShouldNotReleaseSoldTickets() {
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

        when(ticketRepository.findByBookingIdForUpdate(10L))
                .thenReturn(List.of(ticket1, ticket2));

        when(ticket1.getStatus()).thenReturn(TicketStatus.SOLD);
        when(ticket2.getStatus()).thenReturn(TicketStatus.SOLD);

        when(adminRefundService.initiateRefund(
                bookingReference,
                "Customer cancellation"
        )).thenReturn(new RefundResponse(
                1L,
                bookingReference,
                new BigDecimal("1200.00"),
                "REFUND_PENDING",
                "Customer cancellation",
                null,
                Instant.now(),
                Instant.now()
        ));

        RefundResponse response = cancellationService.cancelPaidBooking(
                bookingReference,
                "user@test.com"
        );

        assertEquals("REFUND_PENDING", response.status());

        verify(ticketRepository, never())
                .releaseTicketsForBooking(anyLong(), anyList());
    }

    @Test
    void cancellationShouldRejectAnotherUsersBooking() {
        when(user.getEmail()).thenReturn("owner@test.com");

        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancelPaidBooking(
                        bookingReference,
                        "other@test.com"
                )
        );

        verify(ticketRepository, never())
                .findByBookingIdForUpdate(anyLong());

        verifyNoInteractions(adminRefundService);
    }

    @Test
    void cancellationShouldRejectNonConfirmedBooking() {
        when(booking.getStatus()).thenReturn(BookingStatus.PENDING);

        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancelPaidBooking(
                        bookingReference,
                        "user@test.com"
                )
        );

        verifyNoInteractions(adminRefundService);
        verify(ticketRepository, never())
                .releaseTicketsForBooking(anyLong(), anyList());
    }

    @Test
    void cancellationShouldRejectUsedTickets() {
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

        when(ticketRepository.findByBookingIdForUpdate(10L))
                .thenReturn(List.of(ticket1, ticket2));

        when(ticket1.getStatus()).thenReturn(TicketStatus.SOLD);
        when(ticket2.getStatus()).thenReturn(TicketStatus.USED);

        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancelPaidBooking(
                        bookingReference,
                        "user@test.com"
                )
        );

        verifyNoInteractions(adminRefundService);

        verify(ticketRepository, never())
                .releaseTicketsForBooking(anyLong(), anyList());
    }

    @Test
    void cancellationShouldRejectIncompleteTicketInventory() {
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

        when(ticketRepository.findByBookingIdForUpdate(10L))
                .thenReturn(List.of(ticket1));

        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancelPaidBooking(
                        bookingReference,
                        "user@test.com"
                )
        );

        verifyNoInteractions(adminRefundService);
    }

    @Test
    void cancellationShouldRejectDuplicateRequest() {
        when(booking.getStatus()).thenReturn(BookingStatus.REFUND_PENDING);

        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancelPaidBooking(
                        bookingReference,
                        "user@test.com"
                )
        );

        verifyNoInteractions(adminRefundService);
    }
}