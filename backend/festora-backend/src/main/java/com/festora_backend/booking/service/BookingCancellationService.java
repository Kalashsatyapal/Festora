
package com.festora_backend.booking.service;

import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.admin.service.AdminRefundService;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.TicketStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookingCancellationService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final AdminRefundService adminRefundService;

    public BookingCancellationService(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            AdminRefundService adminRefundService
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.adminRefundService = adminRefundService;
    }

    @Transactional
    public RefundResponse cancelPaidBooking(
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

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(
                    "Only confirmed bookings can be cancelled through this request"
            );
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
                ticket -> ticket.getStatus() != TicketStatus.SOLD
        )) {
            throw new BusinessException(
                    "This booking contains tickets that cannot be refunded"
            );
        }

        booking.cancel();

        RefundResponse response = adminRefundService.initiateRefund(
                bookingReference,
                "Customer cancellation"
        );

        if ("REFUNDED".equalsIgnoreCase(response.status())) {
            int released = ticketRepository.releaseTicketsForBooking(
                    booking.getId(),
                    List.of(TicketStatus.SOLD)
            );

            if (released != booking.getQuantity()) {
                throw new BusinessException(
                        "Refund completed but ticket release requires recovery"
                );
            }
        }

        return response;
    }
}
