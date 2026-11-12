package com.festora_backend.booking.repository;

import java.math.BigDecimal;

public interface OrganizerRevenueProjection {

    BigDecimal getTotalRevenue();

    Long getConfirmedBookings();

    Long getTicketsSold();
}