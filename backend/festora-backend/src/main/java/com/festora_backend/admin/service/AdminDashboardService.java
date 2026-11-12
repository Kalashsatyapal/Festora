package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.AdminDashboardResponse;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final RefundAuditRepository refundRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            RefundAuditRepository refundRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.refundRepository = refundRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        Long totalUsers = userRepository.countByRole(UserRole.USER);
        Long totalOrganizers = userRepository.countByRole(UserRole.ORGANIZER);

        Long pendingOrganizers = (long) userRepository
                .findByRoleAndStatus(
                        UserRole.ORGANIZER,
                        AccountStatus.PENDING
                )
                .size();

        Long activeEvents = eventRepository.countByStatus(
                EventStatus.PUBLISHED
        );

        Long draftEvents = eventRepository.countByStatus(
                EventStatus.DRAFT
        );

        Long confirmedBookings = bookingRepository.countByStatus(
                BookingStatus.CONFIRMED
        );

        Long cancelledBookings = bookingRepository.countByStatus(
                BookingStatus.CANCELLED
        );

        BigDecimal totalRevenue =
                bookingRepository.sumTotalAmountByStatus(
                        BookingStatus.CONFIRMED
                );

        BigDecimal pendingRefundAmount =
                refundRepository.sumAmountByStatus(
                        PaymentStatus.REFUND_PENDING
                );

        Long ticketsSold = bookingRepository.sumQuantityByStatus(
                BookingStatus.CONFIRMED
        );

        return new AdminDashboardResponse(
                totalUsers,
                totalOrganizers,
                pendingOrganizers,
                activeEvents,
                draftEvents,
                confirmedBookings,
                cancelledBookings,
                ticketsSold,
                totalRevenue == null ? BigDecimal.ZERO : totalRevenue,
                pendingRefundAmount == null
                        ? BigDecimal.ZERO
                        : pendingRefundAmount
        );
    }
}