package com.festora_backend.admin.controller;

import com.festora_backend.admin.dto.BookingAdminResponse;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.common.response.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final BookingRepository bookingRepository;

    public AdminBookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingAdminResponse>>> getBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        String keyword = search == null || search.isBlank()
                ? null
                : search.trim();

        Page<Booking> bookings = bookingRepository.searchAdminBookings(
                status,
                keyword,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, Booking::getCreatedAt)
                )
        );

        PageResponse<BookingAdminResponse> response = PageResponse.from(
                bookings.map(BookingAdminResponse::from)
        );

        return ResponseEntity.ok(
                ApiResponse.success("Bookings retrieved successfully", response)
        );
    }
}