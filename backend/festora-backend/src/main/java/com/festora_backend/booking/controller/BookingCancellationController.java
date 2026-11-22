package com.festora_backend.booking.controller;

import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.booking.service.BookingCancellationService;
import com.festora_backend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingCancellationController {

    private final BookingCancellationService cancellationService;

    public BookingCancellationController(
            BookingCancellationService cancellationService
    ) {
        this.cancellationService = cancellationService;
    }

    @PostMapping("/{bookingReference}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<RefundResponse>> cancelBooking(
            @PathVariable UUID bookingReference,
            Authentication authentication
    ) {
        RefundResponse response = cancellationService.cancelBooking(
                bookingReference,
                authentication.getName()
        );

        String message = switch (response.status().toUpperCase(Locale.ROOT)) {
            case "CANCELLED" -> "Pending booking cancelled successfully";
            case "REFUNDED" -> "Booking cancelled and refund completed";
            default -> "Cancellation accepted. Refund is being processed";
        };

        return ResponseEntity.ok(
                ApiResponse.success(message, response)
        );
    }
}