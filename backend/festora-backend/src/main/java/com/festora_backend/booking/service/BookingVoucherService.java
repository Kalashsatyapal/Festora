
package com.festora_backend.booking.service;

import com.festora_backend.booking.dto.BookingVoucherResponse;
import com.festora_backend.booking.dto.TicketVoucherResponse;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import com.festora_backend.ticket.service.TicketQrService;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookingVoucherService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final MessageSource messageSource;
    private final TicketQrService ticketQrService;

    public BookingVoucherService(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            MessageSource messageSource,
            TicketQrService ticketQrService
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.messageSource = messageSource;
        this.ticketQrService = ticketQrService;
    }

    public BookingVoucherResponse generateVoucher(
            UUID bookingReference,
            Long userId,
            Locale locale
    ) {
        Booking booking = bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking",
                                bookingReference
                        )
                );

        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(
                    "You cannot access another user's voucher"
            );
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(
                    "Voucher is available only after payment confirmation"
            );
        }

        List<Ticket> tickets = ticketRepository
                .findByBookingId(booking.getId())
                .stream()
                .sorted(Comparator.comparing(Ticket::getSeatNumber))
                .toList();

        if (tickets.isEmpty()
                || tickets.stream().anyMatch(ticket ->
                        ticket.getStatus() != TicketStatus.SOLD
                        && ticket.getStatus() != TicketStatus.USED
                )) {
            throw new BusinessException(
                    "Confirmed booking ticket data is incomplete"
            );
        }

        List<TicketVoucherResponse> ticketResponses =
                tickets.stream()
                        .map(ticket -> {
                            String ticketNumber =
                                    ticket.getTicketNumber().toString();

                            return new TicketVoucherResponse(
                                    ticketNumber,
                                    ticket.getSeatNumber(),
                                    ticket.getStatus().name(),
                                    ticketQrService.generateDataUri(
                                            ticketNumber
                                    )
                            );
                        })
                        .toList();

        return new BookingVoucherResponse(
                message("voucher.title", locale),
                message("voucher.booking.reference", locale),
                message("voucher.event.id", locale),
                message("voucher.ticket.number", locale),
                message("voucher.seat.number", locale),
                booking.getBookingReference().toString(),
                booking.getEvent().getId(),
                Instant.now(),
                ticketResponses
        );
    }

    private String message(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }
}
