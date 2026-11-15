package com.festora_backend.booking.controller;

import com.festora_backend.booking.dto.BookingSummaryResponse;
import com.festora_backend.booking.service.BookingQueryService;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.common.response.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('USER')")
public class BookingQueryController {

    private final BookingQueryService bookingQueryService;

    public BookingQueryController(BookingQueryService bookingQueryService) {
        this.bookingQueryService = bookingQueryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingSummaryResponse>>> getMyBookings(
            Authentication authentication,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Bookings retrieved successfully",
                bookingQueryService.getMyBookings(
                        authentication.getName(),
                        status,
                        page,
                        size
                )
        ));
    }
}