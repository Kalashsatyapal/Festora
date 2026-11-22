package com.festora_backend.booking.service;

import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.admin.service.AdminRefundService;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.event.BookingCancelledEvent;
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
public class BookingCancellationService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final AdminRefundService adminRefundService;
    private final ApplicationEventPublisher eventPublisher;

    public BookingCancellationService(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            AdminRefundService adminRefundService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.adminRefundService = adminRefundService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RefundResponse cancelBooking(
            UUID bookingReference,
            String email
    ) {
        Booking booking = bookingRepository
                .findByBookingReferenceForUpdate(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking", bookingReference)
                );

        if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException(
                    "You cannot cancel another user's booking"
            );
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            return cancelPendingBooking(booking);
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return cancelConfirmedBooking(booking);
        }

        throw new BusinessException(
                "This booking cannot be cancelled"
        );
    }

    private RefundResponse cancelPendingBooking(Booking booking) {
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
                    "This booking contains tickets that cannot be released"
            );
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

        Instant now = Instant.now();

        return new RefundResponse(
                null,
                booking.getBookingReference(),
                booking.getTotalAmount(),
                "CANCELLED",
                "Customer cancellation",
                null,
                now,
                now
        );
    }

    private RefundResponse cancelConfirmedBooking(Booking booking) {
        List<Ticket> tickets = ticketRepository.findByBookingIdForUpdate(
                booking.getId()
        );

        if (tickets.size() != booking.getQuantity()) {
            throw new BusinessException(
                    "Booking ticket inventory is inconsistent"
            );
        }

        if (tickets.stream().anyMatch(
                ticket -> ticket.getStatus() != TicketStatus.SOLD
        )) {
            throw new BusinessException(
                    "This booking contains tickets that cannot be refunded"
            );
        }

        booking.cancel();

        return adminRefundService.initiateRefund(
                booking.getBookingReference(),
                "Customer cancellation"
        );
    }
}