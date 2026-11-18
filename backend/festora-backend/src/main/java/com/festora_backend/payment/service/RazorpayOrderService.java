
package com.festora_backend.payment.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.payment.dto.RazorpayOrderResponse;
import com.festora_backend.payment.entity.PaymentRecord;
import com.festora_backend.payment.repository.PaymentRecordRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RazorpayOrderService {

    private final BookingRepository bookingRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayOrderService(
            BookingRepository bookingRepository,
            PaymentRecordRepository paymentRecordRepository,
            RestClient.Builder restClientBuilder,
            @Value("${festora.razorpay.key-id}") String keyId,
            @Value("${festora.razorpay.key-secret}") String keySecret
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.restClient = restClientBuilder
                .baseUrl("https://api.razorpay.com")
                .build();
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Transactional
    public RazorpayOrderResponse createOrder(
            UUID bookingReference,
            String email
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
                    "You cannot create a payment for another user's booking"
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(
                    "This booking is not awaiting payment"
            );
        }

        if (!booking.getExpiresAt().isAfter(Instant.now())) {
            throw new BusinessException(
                    "The booking reservation has expired"
            );
        }

        var existingPayment = paymentRecordRepository
                .findByBooking_Id(booking.getId());

        if (existingPayment.isPresent()) {
            PaymentRecord payment = existingPayment.get();

            return response(
                    booking,
                    payment.getRazorpayOrderId(),
                    payment.getAmountPaise(),
                    payment.getCurrency()
            );
        }

        BigDecimal totalAmount = booking.getTotalAmount();
        long amountPaise;

        try {
            amountPaise = totalAmount
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    "Booking amount is invalid for payment"
            );
        }

        if (amountPaise < 100) {
            throw new BusinessException(
                    "The minimum payment amount is ₹1.00"
            );
        }

        Map<String, Object> request = Map.of(
                "amount", amountPaise,
                "currency", "INR",
                "receipt", booking.getBookingReference().toString(),
                "notes", Map.of(
                        "booking_reference",
                        booking.getBookingReference().toString()
                )
        );

        RazorpayApiOrder razorpayOrder;

        try {
            razorpayOrder = restClient.post()
                    .uri("/v1/orders")
                    .headers(headers ->
                            headers.setBasicAuth(keyId, keySecret)
                    )
                    .body(request)
                    .retrieve()
                    .body(RazorpayApiOrder.class);
        } catch (RestClientException exception) {
            throw new BusinessException(
                    "Unable to create payment order. Please try again."
            );
        }

        if (razorpayOrder == null
                || razorpayOrder.id() == null
                || razorpayOrder.id().isBlank()
                || !Long.valueOf(amountPaise).equals(razorpayOrder.amount())
                || !"INR".equals(razorpayOrder.currency())) {
            throw new BusinessException(
                    "Payment gateway returned an invalid order"
            );
        }

        PaymentRecord payment = PaymentRecord.create(
                booking,
                razorpayOrder.id(),
                amountPaise,
                "INR"
        );

        paymentRecordRepository.save(payment);

        return response(
                booking,
                razorpayOrder.id(),
                amountPaise,
                "INR"
        );
    }

    private RazorpayOrderResponse response(
            Booking booking,
            String orderId,
            Long amount,
            String currency
    ) {
        return new RazorpayOrderResponse(
                booking.getBookingReference(),
                orderId,
                amount,
                currency,
                keyId,
                booking.getExpiresAt()
        );
    }

    private record RazorpayApiOrder(
            @JsonProperty("id") String id,
            @JsonProperty("amount") Long amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("status") String status
    ) {
    }
}
