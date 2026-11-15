package com.festora_backend.booking.service;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.event.BookingCancelledEvent;
import com.festora_backend.booking.event.BookingConfirmedEvent;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.event.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingLifecycleServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Booking booking;

    @Mock
    private User user;

    @Mock
    private Event event;

    @Mock
    private Ticket ticket1;

    @Mock
    private Ticket ticket2;

    private BookingLifecycleService lifecycleService;
    private UUID bookingReference;

    @BeforeEach
    void setUp() {
        lifecycleService = new BookingLifecycleService(
                bookingRepository,
                ticketRepository,
                eventPublisher
        );

        bookingReference = UUID.randomUUID();

        when(bookingRepository.findByBookingReferenceForUpdate(bookingReference))
                .thenReturn(Optional.of(booking));

        when(booking.getId()).thenReturn(10L);
        when(booking.getBookingReference()).thenReturn(bookingReference);
        when(booking.getQuantity()).thenReturn(2);
        when(booking.getUser()).thenReturn(user);
        when(booking.getEvent()).thenReturn(event);
        when(user.getId()).thenReturn(20L);
        when(event.getId()).thenReturn(30L);
    }

    @Test
    void cancellationShouldReleaseAllBookingTickets() {
        when(booking.getStatus()).thenReturn(BookingStatus.PENDING);

        when(ticketRepository.releaseTicketsForBooking(
                eq(10L),
                eq(List.of(TicketStatus.RESERVED, TicketStatus.SOLD))
        )).thenReturn(2);

        lifecycleService.cancelBooking(
                bookingReference,
                "user@test.com",
                20L
        );

        verify(booking).cancel();

        verify(ticketRepository).releaseTicketsForBooking(
                10L,
                List.of(TicketStatus.RESERVED, TicketStatus.SOLD)
        );

        verify(eventPublisher).publishEvent(
                new BookingCancelledEvent(bookingReference, 20L, 30L)
        );
    }

    @Test
    void cancellationShouldFailWhenNotAllTicketsAreReleased() {
        when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

        when(ticketRepository.releaseTicketsForBooking(
                eq(10L),
                anyList()
        )).thenReturn(1);

        assertThrows(
                BusinessException.class,
                () -> lifecycleService.cancelBooking(
                        bookingReference,
                        "user@test.com",
                        20L
                )
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void cancellationShouldRejectAnotherUsersBooking() {
        when(booking.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(99L);

        assertThrows(
                BusinessException.class,
                () -> lifecycleService.cancelBooking(
                        bookingReference,
                        "other@test.com",
                        20L
                )
        );

        verify(booking, never()).cancel();
        verify(ticketRepository, never()).releaseTicketsForBooking(anyLong(), anyList());
    }

    @Test
    void confirmationShouldConvertReservedTicketsToSold() {
        when(booking.getStatus()).thenReturn(BookingStatus.PENDING);

        when(ticketRepository.findByBookingIdForUpdate(10L))
                .thenReturn(List.of(ticket1, ticket2));

        when(ticket1.getStatus()).thenReturn(TicketStatus.RESERVED);
        when(ticket2.getStatus()).thenReturn(TicketStatus.RESERVED);

        lifecycleService.confirmAfterPayment(bookingReference);

        verify(booking).confirm();
        verify(ticket1).confirmSale();
        verify(ticket2).confirmSale();
        verify(ticketRepository).saveAll(List.of(ticket1, ticket2));

        verify(eventPublisher).publishEvent(
                new BookingConfirmedEvent(bookingReference, 20L, 30L)
        );
    }

    @Test
    void confirmationShouldRejectIncompleteTicketInventory() {
        when(booking.getStatus()).thenReturn(BookingStatus.PENDING);

        when(ticketRepository.findByBookingIdForUpdate(10L))
                .thenReturn(List.of(ticket1));

        assertThrows(
                BusinessException.class,
                () -> lifecycleService.confirmAfterPayment(bookingReference)
        );

        verify(booking, never()).confirm();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void confirmationShouldRejectNonPendingBooking() {
        when(booking.getStatus()).thenReturn(BookingStatus.CANCELLED);

        assertThrows(
                BusinessException.class,
                () -> lifecycleService.confirmAfterPayment(bookingReference)
        );

        verify(ticketRepository, never()).findByBookingIdForUpdate(anyLong());
    }
}