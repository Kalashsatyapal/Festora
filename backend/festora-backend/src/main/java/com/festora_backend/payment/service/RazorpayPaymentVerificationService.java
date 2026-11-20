
package com.festora_backend.payment.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.festora_backend.admin.service.AdminRefundService;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.booking.service.BookingLifecycleService;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.payment.dto.PaymentVerificationRequest;
import com.festora_backend.payment.dto.PaymentVerificationResponse;
import com.festora_backend.payment.entity.PaymentRecord;
import com.festora_backend.payment.repository.PaymentRecordRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RazorpayPaymentVerificationService {

    private final BookingRepository bookingRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final BookingLifecycleService bookingLifecycleService;
    private final AdminRefundService adminRefundService;
    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayPaymentVerificationService(
            BookingRepository bookingRepository,
            PaymentRecordRepository paymentRecordRepository,
            BookingLifecycleService bookingLifecycleService,
            AdminRefundService adminRefundService,
            RestClient.Builder restClientBuilder,
            @Value("${festora.razorpay.key-id}") String keyId,
            @Value("${festora.razorpay.key-secret}") String keySecret
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.bookingLifecycleService = bookingLifecycleService;
        this.adminRefundService = adminRefundService;
        this.restClient = restClientBuilder
                .baseUrl("https://api.razorpay.com")
                .build();
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Transactional
    public PaymentVerificationResponse verifyPayment(
            UUID bookingReference,
            String email,
            PaymentVerificationRequest request
    ) {
        if (keyId == null || keyId.isBlank()
                || keySecret == null || keySecret.isBlank()) {
            throw new BusinessException(
                    "Payment gateway is not configured"
            );
        }

        Booking booking = bookingRepository
                .findByBookingReferenceForUpdate(bookingReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking",
                                bookingReference
                        )
                );

        if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException(
                    "You cannot verify another user's payment"
            );
        }

        PaymentRecord payment = paymentRecordRepository
                .findByBooking_Id(booking.getId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Payment order was not found for this booking"
                        )
                );

        verifySignature(
                payment.getRazorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (!request.razorpayPaymentId()
                    .equals(payment.getRazorpayPaymentId())) {
                throw new BusinessException(
                        "Payment verification state is inconsistent"
                );
            }

            processSuccessfulPayment(booking);

            return response(booking, payment);
        }

        RazorpayPayment razorpayPayment = fetchPayment(
                request.razorpayPaymentId()
        );

        if (razorpayPayment == null
                || !request.razorpayPaymentId().equals(razorpayPayment.id())
                || !payment.getRazorpayOrderId().equals(razorpayPayment.orderId())
                || !payment.getAmountPaise().equals(razorpayPayment.amount())
                || !payment.getCurrency().equals(razorpayPayment.currency())) {
            throw new BusinessException(
                    "Payment details do not match the booking order"
            );
        }

        if (!"captured".equalsIgnoreCase(razorpayPayment.status())) {
            throw new BusinessException(
                    "Payment has not been captured yet"
            );
        }

        payment.markSuccessful(request.razorpayPaymentId());

        processSuccessfulPayment(booking);

        return response(booking, payment);
    }

    private void processSuccessfulPayment(Booking booking) {
        BookingStatus status = booking.getStatus();

        if (status == BookingStatus.CONFIRMED
                || status == BookingStatus.REFUNDED) {
            return;
        }

        if (status == BookingStatus.PENDING) {
            if (booking.getExpiresAt().isAfter(Instant.now())) {
                bookingLifecycleService.confirmAfterPayment(
                        booking.getBookingReference()
                );
                return;
            }

            bookingLifecycleService.markExpiredPaymentForRefund(
                    booking.getBookingReference()
            );
        } else if (status == BookingStatus.EXPIRED) {
            bookingLifecycleService.markExpiredPaymentForRefund(
                    booking.getBookingReference()
            );
        } else if (status != BookingStatus.REFUND_PENDING
                && status != BookingStatus.CANCELLED) {
            throw new BusinessException(
                    "Booking cannot be processed for captured payment recovery"
            );
        }

        adminRefundService.initiateRefund(
                booking.getBookingReference(),
                "Payment captured after reservation expired or booking was cancelled"
        );
    }

    private void verifySignature(
            String orderId,
            String paymentId,
            String receivedSignature
    ) {
        if (receivedSignature == null
                || !receivedSignature.matches("(?i)^[a-f0-9]{64}$")) {
            throw new BusinessException(
                    "Invalid payment signature"
            );
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            byte[] digest = mac.doFinal(
                    (orderId + "|" + paymentId)
                            .getBytes(StandardCharsets.UTF_8)
            );

            String expectedSignature = HexFormat.of().formatHex(digest);

            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.US_ASCII),
                    receivedSignature.toLowerCase()
                            .getBytes(StandardCharsets.US_ASCII)
            );

            if (!valid) {
                throw new BusinessException(
                        "Payment signature verification failed"
                );
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(
                    "Unable to verify payment signature"
            );
        }
    }

    private RazorpayPayment fetchPayment(String paymentId) {
        try {
            return restClient.get()
                    .uri("/v1/payments/{paymentId}", paymentId)
                    .headers(headers ->
                            headers.setBasicAuth(keyId, keySecret)
                    )
                    .retrieve()
                    .body(RazorpayPayment.class);
        } catch (RestClientException exception) {
            throw new BusinessException(
                    "Unable to confirm payment status with Razorpay"
            );
        }
    }

    private PaymentVerificationResponse response(
            Booking booking,
            PaymentRecord payment
    ) {
        return new PaymentVerificationResponse(
                booking.getBookingReference(),
                payment.getRazorpayPaymentId(),
                payment.getStatus().name(),
                booking.getStatus().name()
        );
    }

    private record RazorpayPayment(
            @JsonProperty("id") String id,
            @JsonProperty("order_id") String orderId,
            @JsonProperty("amount") Long amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("status") String status
    ) {
    }
}
