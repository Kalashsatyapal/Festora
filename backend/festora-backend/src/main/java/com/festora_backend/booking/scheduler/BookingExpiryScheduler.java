package com.festora_backend.booking.scheduler;

import com.festora_backend.booking.service.BookingLifecycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpiryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private final BookingLifecycleService lifecycleService;

    public BookingExpiryScheduler(
            BookingLifecycleService lifecycleService
    ) {
        this.lifecycleService = lifecycleService;
    }

    @Scheduled(
            cron = "${festora.booking.expiry-cron:0 * * * * *}"
    )
    public void expireUnpaidBookings() {

        int count = lifecycleService.expirePendingBookings();

        if (count > 0) {
            log.info(
                    "Expired {} unpaid bookings and released their tickets",
                    count
            );
        }
    }
}