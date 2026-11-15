
package com.festora_backend.ticket.service;

import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.event.entity.Event;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.ticket.dto.SeatAvailabilityResponse;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketInventoryService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketInventoryService(
            TicketRepository ticketRepository,
            EventRepository eventRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void initializeForEvent(Event event) {
        long existingTickets = ticketRepository.countByEventId(event.getId());

        if (existingTickets == event.getTotalCapacity()) {
            return;
        }

        if (existingTickets != 0) {
            throw new BusinessException(
                    "Event ticket inventory is incomplete"
            );
        }

        List<Ticket> tickets = new ArrayList<>(event.getTotalCapacity());

        for (int seat = 1; seat <= event.getTotalCapacity(); seat++) {
            tickets.add(
                    Ticket.create(
                            event,
                            String.valueOf(seat),
                            event.getTicketPrice()
                    )
            );
        }

        ticketRepository.saveAll(tickets);
    }

    @Transactional(readOnly = true)
    public List<SeatAvailabilityResponse> getAvailability(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Published event",
                                eventId
                        )
                );

        return ticketRepository.findByEventIdOrderByIdAsc(event.getId())
                .stream()
                .map(ticket -> new SeatAvailabilityResponse(
                        ticket.getSeatNumber(),
                        ticket.getPrice(),
                        ticket.getStatus(),
                        ticket.getReservedUntil()
                ))
                .toList();
    }
}
