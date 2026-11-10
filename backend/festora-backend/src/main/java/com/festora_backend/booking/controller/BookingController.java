package com.festora_backend.booking.controller;
import com.festora_backend.booking.dto.CheckoutRequest;
import com.festora_backend.booking.dto.CheckoutResponse;
import com.festora_backend.booking.service.BookingService;
import com.festora_backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication
    ) {
        CheckoutResponse response = bookingService.checkout(
                request,
                authentication.getName()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Tickets reserved successfully. Complete payment within 15 minutes.",
                        response
                ));
    }
}