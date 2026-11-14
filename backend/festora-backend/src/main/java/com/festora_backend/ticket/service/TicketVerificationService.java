
package com.festora_backend.ticket.service;

import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.ticket.dto.TicketVerificationResponse;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketVerificationService {

    private final TicketRepository ticketRepository;

    public TicketVerificationService(
            TicketRepository ticketRepository
    ) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketVerificationResponse verify(
            UUID ticketNumber,
            String organizerEmail
    ) {
        Ticket ticket = ticketRepository
                .findByTicketNumberForUpdate(ticketNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket",
                                ticketNumber
                        )
                );

        if (!ticket.getEvent().getOrganizer()
                .getEmail().equalsIgnoreCase(organizerEmail)) {
            throw new BusinessException(
                    "You cannot verify tickets for another organizer's event"
            );
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new BusinessException(
                    "Ticket has already been used"
            );
        }

        if (ticket.getStatus() != TicketStatus.SOLD) {
            throw new BusinessException(
                    "Ticket is not valid for entry"
            );
        }

        ticket.markUsed();

        ticketRepository.saveAndFlush(ticket);

        return new TicketVerificationResponse(
                ticket.getTicketNumber().toString(),
                ticket.getEvent().getTitle(),
                ticket.getSeatNumber(),
                ticket.getStatus().name(),
                ticket.getUsedAt()
        );
    }
}
