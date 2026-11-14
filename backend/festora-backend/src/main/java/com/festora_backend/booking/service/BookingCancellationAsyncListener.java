package com.festora_backend.booking.service;

import com.festora_backend.booking.event.BookingCancelledEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingCancellationAsyncListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BookingCancellationAsyncListener.class
            );

    private final BookingEmailService bookingEmailService;

    public BookingCancellationAsyncListener(
            BookingEmailService bookingEmailService
    ) {
        this.bookingEmailService = bookingEmailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCancellation(BookingCancelledEvent event) {
        try {
            bookingEmailService.sendBookingCancellation(
                    event.bookingReference()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to send cancellation email for booking {}",
                    event.bookingReference(),
                    exception
            );
        }
    }
}