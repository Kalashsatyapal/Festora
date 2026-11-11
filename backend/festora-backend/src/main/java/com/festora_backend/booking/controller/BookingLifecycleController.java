package com.festora_backend.booking.controller;

import com.festora_backend.booking.dto.BookingVoucherResponse;
import com.festora_backend.booking.service.BookingLifecycleService;
import com.festora_backend.booking.service.BookingVoucherService;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/{bookingReference}")
public class BookingLifecycleController {

    private final BookingLifecycleService lifecycleService;
    private final BookingVoucherService voucherService;
    private final UserRepository userRepository;

    public BookingLifecycleController(
            BookingLifecycleService lifecycleService,
            BookingVoucherService voucherService,
            UserRepository userRepository
    ) {
        this.lifecycleService = lifecycleService;
        this.voucherService = voucherService;
        this.userRepository = userRepository;
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable UUID bookingReference,
            Authentication authentication
    ) {

        User user = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found")
                );

        lifecycleService.cancelBooking(
                bookingReference,
                authentication.getName(),
                user.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Booking cancelled and ticket inventory released",
                        null
                )
        );
    }

    @GetMapping("/voucher")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingVoucherResponse>> getVoucher(
            @PathVariable UUID bookingReference,
            Authentication authentication
    ) {

        User user = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found")
                );

        Locale locale = LocaleContextHolder.getLocale();

        BookingVoucherResponse voucher =
                voucherService.generateVoucher(
                        bookingReference,
                        user.getId(),
                        locale
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Digital ticket voucher generated",
                        voucher
                )
        );
    }
}