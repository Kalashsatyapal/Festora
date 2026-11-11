package com.festora_backend.booking.service;

import com.festora_backend.booking.event.BookingCancelledEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingCancellationAsyncListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BookingCancellationAsyncListener.class
            );

    @Async
    @TransactionalEventListener
    public void handleCancellation(
            BookingCancelledEvent event
    ) {
        log.info(
                "Booking cancellation processed asynchronously. Reference={}, User={}, Event={}",
                event.bookingReference(),
                event.userId(),
                event.eventId()
        );
    }
}