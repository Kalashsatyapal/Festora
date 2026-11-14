package com.festora_backend.booking.service;

import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.ticket.entity.Ticket;
import com.festora_backend.ticket.repository.TicketRepository;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookingEmailService {

    private final JavaMailSender mailSender;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    public BookingEmailService(
            JavaMailSender mailSender,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository
    ) {
        this.mailSender = mailSender;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public void sendBookingConfirmation(UUID bookingReference) {
        Booking booking = findBooking(bookingReference);

        List<Ticket> tickets = ticketRepository.findByBookingId(
                booking.getId()
        );

        String seats = tickets.stream()
                .map(Ticket::getSeatNumber)
                .sorted()
                .reduce((first, second) -> first + ", " + second)
                .orElse("Not available");

        String ticketNumbers = tickets.stream()
                .map(ticket -> ticket.getTicketNumber().toString())
                .reduce((first, second) -> first + "\n" + second)
                .orElse("Not available");

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(booking.getUser().getEmail());
        message.setSubject(
                "Festora Booking Confirmed - "
                        + booking.getBookingReference()
        );

        message.setText(
                "Hello " + booking.getUser().getFullName() + ",\n\n"
                        + "Your Festora booking is confirmed.\n\n"
                        + "Event: " + booking.getEvent().getTitle() + "\n"
                        + "Booking Reference: "
                        + booking.getBookingReference() + "\n"
                        + "Seats: " + seats + "\n"
                        + "Ticket Numbers:\n" + ticketNumbers + "\n\n"
                        + "Amount: ₹" + booking.getTotalAmount() + "\n\n"
                        + "Please keep your digital tickets ready at the venue.\n\n"
                        + "Thank you for booking with Festora."
        );

        mailSender.send(message);
    }

    @Transactional(readOnly = true)
    public void sendBookingCancellation(UUID bookingReference) {
        Booking booking = findBooking(bookingReference);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(booking.getUser().getEmail());
        message.setSubject(
                "Festora Booking Cancelled - "
                        + booking.getBookingReference()
        );

        message.setText(
                "Hello " + booking.getUser().getFullName() + ",\n\n"
                        + "Your Festora booking has been cancelled.\n\n"
                        + "Event: " + booking.getEvent().getTitle() + "\n"
                        + "Booking Reference: "
                        + booking.getBookingReference() + "\n"
                        + "Booking Amount: ₹" + booking.getTotalAmount()
                        + "\n\n"
                        + "If a payment was completed, any applicable refund "
                        + "will be processed according to the refund status.\n\n"
                        + "Thank you,\nFestora Support"
        );

        mailSender.send(message);
    }

    private Booking findBooking(UUID bookingReference) {
        return bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking",
                                bookingReference
                        )
                );
    }
}