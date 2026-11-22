
package com.festora_backend.booking.service;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.event.BookingCancelledEvent;
import com.festora_backend.booking.event.BookingConfirmedEvent;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookingLifecycleService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BookingLifecycleService(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void confirmAfterPayment(UUID bookingReference) {
        Booking booking = bookingRepository
                .findByBookingReferenceForUpdate(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking", bookingReference)
                );

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Booking is not awaiting payment");
        }

        List<Ticket> tickets = ticketRepository.findByBookingIdForUpdate(
                booking.getId()
        );

        if (tickets.size() != booking.getQuantity()) {
            throw new BusinessException(
                    "Booking ticket inventory is inconsistent"
            );
        }

        if (tickets.stream().anyMatch(
                ticket -> ticket.getStatus() != TicketStatus.RESERVED
        )) {
            throw new BusinessException(
                    "Some reserved tickets are no longer available"
            );
        }

        booking.confirm();

        for (Ticket ticket : tickets) {
            ticket.confirmSale();
        }

        ticketRepository.saveAll(tickets);

        eventPublisher.publishEvent(
                new BookingConfirmedEvent(
                        booking.getBookingReference(),
                        booking.getUser().getId(),
                        booking.getEvent().getId()
                )
        );
    }

    @Transactional
    public void markExpiredPaymentForRefund(UUID bookingReference) {
        Booking booking = bookingRepository
                .findByBookingReferenceForUpdate(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking", bookingReference)
                );

        if (booking.getStatus() == BookingStatus.REFUND_PENDING) {
            return;
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            if (booking.getExpiresAt().isAfter(Instant.now())) {
                throw new BusinessException(
                        "Booking reservation has not expired"
                );
            }

            booking.expire();

            int released = ticketRepository.releaseTicketsForBooking(
                    booking.getId(),
                    List.of(TicketStatus.RESERVED)
            );

            if (released != booking.getQuantity()) {
                throw new BusinessException(
                        "Unable to release all expired booking tickets"
                );
            }

            booking = bookingRepository
                    .findByBookingReferenceForUpdate(bookingReference)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Booking", bookingReference)
                    );
        } else if (booking.getStatus() != BookingStatus.EXPIRED) {
            throw new BusinessException(
                    "Only expired bookings can enter payment recovery"
            );
        }

        booking.markRefundPending();
        bookingRepository.save(booking);
    }

    @Transactional
    public int expirePendingBookings() {
        Instant now = Instant.now();

        List<Booking> expiredCandidates =
                bookingRepository.findByStatusAndExpiresAtBefore(
                        BookingStatus.PENDING,
                        now
                );

        int expiredCount = 0;

        for (Booking candidate : expiredCandidates) {
            Booking booking = bookingRepository
                    .findByBookingReferenceForUpdate(
                            candidate.getBookingReference()
                    )
                    .orElse(null);

            if (booking == null
                    || booking.getStatus() != BookingStatus.PENDING
                    || booking.getExpiresAt().isAfter(Instant.now())) {
                continue;
            }

            booking.expire();

            ticketRepository.releaseTicketsForBooking(
                    booking.getId(),
                    List.of(TicketStatus.RESERVED)
            );

            expiredCount++;
        }

        return expiredCount;
    }

    @Transactional
public void cancelBooking(
        UUID bookingReference,
        String email,
        Long userId
) {
    Booking booking = bookingRepository
            .findByBookingReferenceForUpdate(bookingReference)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Booking", bookingReference)
            );

    if (!booking.getUser().getId().equals(userId)) {
        throw new BusinessException(
                "You cannot cancel another user's booking"
        );
    }

    if (booking.getStatus() == BookingStatus.CONFIRMED) {
        throw new BusinessException(
                "Paid bookings must be refunded before cancellation"
        );
    }

    if (booking.getStatus() != BookingStatus.PENDING) {
        throw new BusinessException("This booking cannot be cancelled");
    }

    booking.cancel();

    int released = ticketRepository.releaseTicketsForBooking(
            booking.getId(),
            List.of(TicketStatus.RESERVED)
    );

    if (released != booking.getQuantity()) {
        throw new BusinessException(
                "Unable to release all booking tickets"
        );
    }

    eventPublisher.publishEvent(
            new BookingCancelledEvent(
                    booking.getBookingReference(),
                    booking.getUser().getId(),
                    booking.getEvent().getId()
            )
    );
}
}
