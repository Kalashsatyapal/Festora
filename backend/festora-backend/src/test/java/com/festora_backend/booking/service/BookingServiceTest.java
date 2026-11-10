package com.festora_backend.booking.service;
import com.festora_backend.booking.dto.CheckoutRequest;
import com.festora_backend.booking.dto.CheckoutResponse;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.event.entity.Event;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @Mock
    private Event event;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                ticketRepository,
                eventRepository,
                userRepository
        );
    }

    @Test
    void checkoutShouldReserveTicketsSuccessfully() {

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(event.getId()).thenReturn(1L);

        when(event.getStatus())
                .thenReturn(EventStatus.PUBLISHED);

        Ticket ticket1 = Ticket.create(
                event,
                "A-01",
                new BigDecimal("500.00")
        );

        Ticket ticket2 = Ticket.create(
                event,
                "A-02",
                new BigDecimal("500.00")
        );

        when(ticketRepository.findAvailableForUpdate(
                eq(1L),
                eq(TicketStatus.AVAILABLE),
                any(Pageable.class)
        )).thenReturn(List.of(ticket1, ticket2));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = bookingService.checkout(
                new CheckoutRequest(1L, 2),
                "user@test.com"
        );

        assertNotNull(response);
        assertEquals(2, response.quantity());
        assertEquals(
                new BigDecimal("1000.00"),
                response.totalAmount()
        );

        assertEquals(TicketStatus.RESERVED, ticket1.getStatus());
        assertEquals(TicketStatus.RESERVED, ticket2.getStatus());

        assertEquals(user, ticket1.getHolder());
        assertEquals(user, ticket2.getHolder());

        assertEquals(
                List.of("A-01", "A-02"),
                response.seatNumbers()
        );

        verify(bookingRepository).save(any(Booking.class));
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    void checkoutShouldFailWhenInventoryIsInsufficient() {

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(event.getStatus())
                .thenReturn(EventStatus.PUBLISHED);

        when(ticketRepository.findAvailableForUpdate(
                eq(1L),
                eq(TicketStatus.AVAILABLE),
                any(Pageable.class)
        )).thenReturn(List.of());

        assertThrows(
                BusinessException.class,
                () -> bookingService.checkout(
                        new CheckoutRequest(1L, 2),
                        "user@test.com"
                )
        );

        verify(bookingRepository, never()).save(any());
        verify(ticketRepository, never()).saveAll(anyList());
    }

    @Test
    void checkoutShouldRejectUnpublishedEvent() {

        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(event.getStatus())
                .thenReturn(EventStatus.DRAFT);

        assertThrows(
                BusinessException.class,
                () -> bookingService.checkout(
                        new CheckoutRequest(1L, 1),
                        "user@test.com"
                )
        );

        verify(ticketRepository, never())
                .findAvailableForUpdate(
                        anyLong(),
                        any(),
                        any(Pageable.class)
                );
    }
}