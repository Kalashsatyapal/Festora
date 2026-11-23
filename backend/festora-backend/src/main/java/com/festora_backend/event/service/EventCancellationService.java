
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventCancellationService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    public EventCancellationService(
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository
    ) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void cancel(Long eventId, String organizerEmail) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event", eventId)
                );

        if (!event.getOrganizer().getEmail()
                .equalsIgnoreCase(organizerEmail)) {
            throw new BusinessException("You do not own this event");
        }

        event.cancel();

        List<Booking> bookings =
                bookingRepository.findByEventIdAndStatusIn(
                        eventId,
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.CONFIRMED
                        )
                );

        for (Booking booking : bookings) {
            BookingStatus previousStatus = booking.getStatus();

            int updated = bookingRepository.updateBookingStatus(
                    booking.getId(),
                    previousStatus,
                    BookingStatus.CANCELLED
            );

            if (updated != 1) {
                throw new BusinessException(
                        "Unable to cancel booking "
                                + booking.getBookingReference()
                );
            }

            if (previousStatus == BookingStatus.PENDING) {
                int released =
                        ticketRepository.releaseTicketsForBooking(
                                booking.getId(),
                                List.of(TicketStatus.RESERVED)
                        );

                if (released != booking.getQuantity()) {
                    throw new BusinessException(
                            "Unable to release all reserved tickets for booking "
                                    + booking.getBookingReference()
                    );
                }
            }
        }
    }
}
