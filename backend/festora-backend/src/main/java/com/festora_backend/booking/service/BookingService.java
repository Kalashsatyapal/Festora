package com.festora_backend.booking.service;
import com.festora_backend.booking.dto.CheckoutRequest;
import com.festora_backend.booking.dto.CheckoutResponse;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.event.entity.Event;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class BookingService {

    private static final Duration RESERVATION_DURATION =
            Duration.ofMinutes(15);

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CheckoutResponse checkout(
            CheckoutRequest request,
            String email
    ) {
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException(
                    "Ticket quantity must be greater than zero"
            );
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", email)
                );

        
        Event event = eventRepository.findByIdForUpdate(
                request.eventId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Event",
                        request.eventId()
                )
        );

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessException(
                    "Tickets are not available for this event"
            );
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(RESERVATION_DURATION);

      Pageable pageable = PageRequest.of(
        0,
        request.quantity(),
        Sort.by(Sort.Direction.ASC, Ticket::getSeatNumber)
);
        List<Ticket> availableTickets =
                ticketRepository.findAvailableForUpdate(
                        event.getId(),
                        TicketStatus.AVAILABLE,
                        pageable
                );

        if (availableTickets.size() < request.quantity()) {
            throw new BusinessException(
                    "Insufficient tickets available"
            );
        }

        BigDecimal totalAmount = availableTickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = Booking.create(
                user,
                event,
                request.quantity(),
                totalAmount,
                expiresAt
        );

        booking = bookingRepository.save(booking);

        for (Ticket ticket : availableTickets) {
            ticket.assignBooking(booking);
            ticket.reserve(user, expiresAt);
        }

        ticketRepository.saveAll(availableTickets);

        List<String> seatNumbers = availableTickets.stream()
                .map(Ticket::getSeatNumber)
                .toList();

        return CheckoutResponse.from(
                booking,
                seatNumbers
        );
    }
}