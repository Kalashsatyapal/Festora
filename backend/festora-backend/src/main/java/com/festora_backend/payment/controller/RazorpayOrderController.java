package com.festora_backend.payment.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.payment.dto.PaymentVerificationRequest;
import com.festora_backend.payment.dto.PaymentVerificationResponse;
import com.festora_backend.payment.dto.RazorpayOrderResponse;
import com.festora_backend.payment.service.RazorpayOrderService;
import com.festora_backend.payment.service.RazorpayPaymentVerificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class RazorpayOrderController {

    private final RazorpayOrderService razorpayOrderService;
    private final RazorpayPaymentVerificationService paymentVerificationService;

    public RazorpayOrderController(
            RazorpayOrderService razorpayOrderService,
            RazorpayPaymentVerificationService paymentVerificationService
    ) {
        this.razorpayOrderService = razorpayOrderService;
        this.paymentVerificationService = paymentVerificationService;
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

    @PostMapping("/{bookingReference}/verify-payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaymentVerificationResponse>> verifyPayment(
            @PathVariable UUID bookingReference,
            @Valid @RequestBody PaymentVerificationRequest request,
            Authentication authentication
    ) {
        PaymentVerificationResponse response =
                paymentVerificationService.verifyPayment(
                        bookingReference,
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment verified and booking confirmed successfully",
                        response
                )
        );
    }
}