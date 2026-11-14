package com.festora_backend.booking.service;

import com.festora_backend.booking.event.BookingConfirmedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingConfirmationAsyncListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BookingConfirmationAsyncListener.class
            );

    private final BookingEmailService bookingEmailService;

    public BookingConfirmationAsyncListener(
            BookingEmailService bookingEmailService
    ) {
        this.bookingEmailService = bookingEmailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConfirmation(BookingConfirmedEvent event) {
        try {
            bookingEmailService.sendBookingConfirmation(
                    event.bookingReference()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to send confirmation email for booking {}",
                    event.bookingReference(),
                    exception
            );
        }
    }
}