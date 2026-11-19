package com.festora.billing.refund;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RefundService(
            RefundRepository refundRepository,
            RestClient.Builder builder,
            @Value("${festora.razorpay.key-id}") String keyId,
            @Value("${festora.razorpay.key-secret}") String keySecret
    ) {
        this.refundRepository = refundRepository;
        this.restClient = builder
                .baseUrl("https://api.razorpay.com")
                .build();
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Transactional
    public RefundResponse process(RefundRequest request) {
        if (keyId == null || keyId.isBlank()
                || keySecret == null || keySecret.isBlank()) {
            throw new IllegalStateException(
                    "Razorpay credentials are not configured"
            );
        }

        Refund refund = refundRepository
                .findByBookingReference(request.bookingReference())
                .orElseGet(() -> refundRepository.saveAndFlush(
                        Refund.pending(
                                request.bookingReference(),
                                request.razorpayPaymentId(),
                                request.amount(),
                                request.reason().trim()
                        )
                ));

        validateExistingRefund(refund, request);

        if (refund.getStatus() == RefundStatus.SUCCEEDED
                || refund.getStatus() == RefundStatus.FAILED) {
            return RefundResponse.from(refund);
        }

        long amountPaise;

        try {
            amountPaise = request.amount()
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException exception) {
            refund.markFailed("Invalid refund amount");
            return RefundResponse.from(refund);
        }

        if (amountPaise < 100) {
            refund.markFailed("Refund amount must be at least ₹1.00");
            return RefundResponse.from(refund);
        }

        Map<String, Object> body = Map.of(
                "amount", amountPaise,
                "receipt", request.bookingReference().toString(),
                "notes", Map.of(
                        "booking_reference",
                        request.bookingReference().toString(),
                        "reason",
                        request.reason().trim()
                )
        );

        try {
            RazorpayRefund response = restClient.post()
                    .uri(
                            "/v1/payments/{paymentId}/refund",
                            request.razorpayPaymentId()
                    )
                    .headers(headers -> {
                        headers.setBasicAuth(keyId, keySecret);
                        headers.set(
                                "X-Refund-Idempotency",
                                request.bookingReference().toString()
                        );
                    })
                    .body(body)
                    .retrieve()
                    .body(RazorpayRefund.class);

            if (response == null
                    || response.id() == null
                    || response.status() == null
                    || response.amount() == null
                    || response.amount() != amountPaise
                    || !request.razorpayPaymentId()
                    .equals(response.paymentId())) {
                refund.markPending();
                return RefundResponse.from(refund);
            }

            switch (response.status().toLowerCase()) {
                case "processed" ->
                        refund.markSucceeded(response.id());
                case "failed" ->
                        refund.markFailed("Razorpay reported refund failure");
                default -> refund.markPending();
            }

        } catch (RestClientResponseException exception) {
            HttpStatusCode status = exception.getStatusCode();

            if (status.is4xxClientError()
                    && status.value() != 409) {
                refund.markFailed(
                        "Razorpay rejected the refund request"
                );
            } else {
                refund.markPending();
            }
        } catch (RestClientException exception) {
            refund.markPending();
        }

        return RefundResponse.from(refund);
    }

    private void validateExistingRefund(
            Refund refund,
            RefundRequest request
    ) {
        if (!refund.getAmount().equals(request.amount())
                || !refund.getRazorpayPaymentId()
                .equals(request.razorpayPaymentId())
                || !refund.getReason().equals(request.reason().trim())) {
            throw new IllegalArgumentException(
                    "A refund already exists with different details"
            );
        }
    }

    private record RazorpayRefund(
            String id,
            Long amount,
            String payment_id,
            String status
    ) {
        public String paymentId() {
            return payment_id;
        }
    }
}