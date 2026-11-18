
package com.festora_backend.payment.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.payment.dto.RazorpayOrderResponse;
import com.festora_backend.payment.service.RazorpayOrderService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class RazorpayOrderController {

    private final RazorpayOrderService razorpayOrderService;

    public RazorpayOrderController(
            RazorpayOrderService razorpayOrderService
    ) {
        this.razorpayOrderService = razorpayOrderService;
    }

    @PostMapping("/{bookingReference}/payment-order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @PathVariable UUID bookingReference,
            Authentication authentication
    ) {
        RazorpayOrderResponse response =
                razorpayOrderService.createOrder(
                        bookingReference,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment order created successfully",
                        response
                )
        );
    }
}
