package com.festora_backend.organizer.service;

import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.booking.repository.OrganizerRevenueProjection;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.event.entity.Event;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.organizer.dto.AttendeeRosterCsvResponse;
import com.festora_backend.organizer.dto.OrganizerRevenueResponse;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizerEventAnalyticsService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    public OrganizerEventAnalyticsService(
            EventRepository eventRepository,
            TicketRepository ticketRepository,
            BookingRepository bookingRepository
    ) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public AttendeeRosterCsvResponse exportAttendees(
            Long eventId,
            Long organizerId
    ) {
        Event event = getOrganizerEvent(eventId, organizerId);

        List<Ticket> tickets =
                ticketRepository.findSoldTicketsWithAttendee(
                        eventId,
                        TicketStatus.SOLD
                );

        StringBuilder csv = new StringBuilder();
        csv.append("ticket_number,booking_reference,attendee_name,attendee_email,seat_number,ticket_price,sold_at\r\n");

        for (Ticket ticket : tickets) {
            csv.append(csvValue(ticket.getTicketNumber().toString())).append(",");
            csv.append(csvValue(ticket.getBooking().getBookingReference().toString())).append(",");
            csv.append(csvValue(ticket.getBooking().getUser().getFullName())).append(",");
            csv.append(csvValue(ticket.getBooking().getUser().getEmail())).append(",");
            csv.append(csvValue(ticket.getSeatNumber())).append(",");
            csv.append(csvValue(ticket.getPrice().toPlainString())).append(",");
            csv.append(csvValue(ticket.getSoldAt() == null ? "" : ticket.getSoldAt().toString()));
            csv.append("\r\n");
        }

        return new AttendeeRosterCsvResponse(
                "event-" + event.getId() + "-attendees.csv",
                "text/csv",
                csv.toString()
        );
    }

    @Transactional(readOnly = true)
    public OrganizerRevenueResponse getEventRevenue(
            Long eventId,
            Long organizerId
    ) {
        Event event = getOrganizerEvent(eventId, organizerId);

        OrganizerRevenueProjection revenue =
                bookingRepository.getOrganizerEventRevenue(
                        eventId,
                        organizerId,
                        BookingStatus.CONFIRMED
                );

        return new OrganizerRevenueResponse(
                event.getId(),
                event.getTitle(),
                revenue.getTotalRevenue(),
                revenue.getConfirmedBookings(),
                revenue.getTicketsSold(),
                "INR"
        );
    }

    private Event getOrganizerEvent(
            Long eventId,
            Long organizerId
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event", eventId)
                );

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new BusinessException(
                    "You do not have access to this event"
            );
        }

        return event;
    }

    private String csvValue(String value) {
        if (value == null) {
            return "\"\"";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}