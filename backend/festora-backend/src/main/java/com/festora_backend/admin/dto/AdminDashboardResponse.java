package com.festora_backend.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        Long totalUsers,
        Long totalOrganizers,
        Long pendingOrganizers,
        Long activeEvents,
        Long draftEvents,
        Long confirmedBookings,
        Long cancelledBookings,
        Long totalTicketsSold,
        BigDecimal totalRevenue,
        BigDecimal pendingRefundAmount) {
}