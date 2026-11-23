
package com.festora_backend.event.service;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.event.entity.Event;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.ticket.repository.TicketRepository;
import com.festora_backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCancellationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private Event event;

    @Mock
    private User organizer;

    @Mock
    private Booking pendingBooking;

    @Mock
    private Booking confirmedBooking;

    private EventCancellationService cancellationService;

    @BeforeEach
    void setUp() {
        cancellationService = new EventCancellationService(
                eventRepository,
                bookingRepository,
                ticketRepository
        );

        lenient().when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        lenient().when(event.getOrganizer()).thenReturn(organizer);
        lenient().when(organizer.getEmail())
                .thenReturn("organizer@test.com");
    }

    @Test
    void shouldCancelEventAndReleasePendingBookingTickets() {
        when(bookingRepository.findByEventIdAndStatusIn(
                1L,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        )).thenReturn(List.of(pendingBooking));

        when(pendingBooking.getId()).thenReturn(10L);
        when(pendingBooking.getQuantity()).thenReturn(2);
        when(pendingBooking.getStatus()).thenReturn(BookingStatus.PENDING);

        when(bookingRepository.updateBookingStatus(
                10L,
                BookingStatus.PENDING,
                BookingStatus.CANCELLED
        )).thenReturn(1);

        when(ticketRepository.releaseTicketsForBooking(
                10L,
                List.of(TicketStatus.RESERVED)
        )).thenReturn(2);

        cancellationService.cancel(1L, "organizer@test.com");

        verify(event).cancel();

        verify(bookingRepository).updateBookingStatus(
                10L,
                BookingStatus.PENDING,
                BookingStatus.CANCELLED
        );

        verify(ticketRepository).releaseTicketsForBooking(
                10L,
                List.of(TicketStatus.RESERVED)
        );
    }

    @Test
    void shouldCancelConfirmedBookingWithoutReleasingSoldTickets() {
        when(bookingRepository.findByEventIdAndStatusIn(
                1L,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        )).thenReturn(List.of(confirmedBooking));

        when(confirmedBooking.getId()).thenReturn(20L);
        when(confirmedBooking.getStatus())
                .thenReturn(BookingStatus.CONFIRMED);

        when(bookingRepository.updateBookingStatus(
                20L,
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED
        )).thenReturn(1);

        cancellationService.cancel(1L, "organizer@test.com");

        verify(event).cancel();

        verify(bookingRepository).updateBookingStatus(
                20L,
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED
        );

        verify(ticketRepository, never())
                .releaseTicketsForBooking(anyLong(), anyList());
    }

    @Test
    void shouldRejectCancellationByAnotherOrganizer() {
        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancel(
                        1L,
                        "another@test.com"
                )
        );

        verify(event, never()).cancel();

        verifyNoInteractions(bookingRepository, ticketRepository);
    }

    @Test
    void shouldRejectAlreadyCancelledEvent() {
        doThrow(new IllegalStateException("Event cannot be cancelled"))
                .when(event).cancel();

        assertThrows(
                IllegalStateException.class,
                () -> cancellationService.cancel(
                        1L,
                        "organizer@test.com"
                )
        );

        verify(bookingRepository, never())
                .findByEventIdAndStatusIn(anyLong(), anyList());

        verifyNoInteractions(ticketRepository);
    }

    @Test
    void shouldFailWhenPendingTicketReleaseIsIncomplete() {
        when(bookingRepository.findByEventIdAndStatusIn(
                1L,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        )).thenReturn(List.of(pendingBooking));

        when(pendingBooking.getId()).thenReturn(30L);
        when(pendingBooking.getQuantity()).thenReturn(3);
        when(pendingBooking.getStatus()).thenReturn(BookingStatus.PENDING);

        when(bookingRepository.updateBookingStatus(
                30L,
                BookingStatus.PENDING,
                BookingStatus.CANCELLED
        )).thenReturn(1);

        when(ticketRepository.releaseTicketsForBooking(
                30L,
                List.of(TicketStatus.RESERVED)
        )).thenReturn(2);

        assertThrows(
                BusinessException.class,
                () -> cancellationService.cancel(
                        1L,
                        "organizer@test.com"
                )
        );

        verify(ticketRepository).releaseTicketsForBooking(
                30L,
                List.of(TicketStatus.RESERVED)
        );
    }

    @Test
    void shouldRejectWhenEventDoesNotExist() {
        when(eventRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cancellationService.cancel(
                        99L,
                        "organizer@test.com"
                )
        );

        verifyNoInteractions(bookingRepository, ticketRepository);
    }
}
