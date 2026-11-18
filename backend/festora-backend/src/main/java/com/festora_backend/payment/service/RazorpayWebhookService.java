package com.festora_backend.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.service.BookingLifecycleService;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.payment.entity.PaymentRecord;
import com.festora_backend.payment.entity.RazorpayWebhookEvent;
import com.festora_backend.payment.repository.PaymentRecordRepository;
import com.festora_backend.payment.repository.RazorpayWebhookEventRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RazorpayWebhookService {

    private final ObjectMapper objectMapper;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RazorpayWebhookEventRepository webhookEventRepository;
    private final BookingLifecycleService bookingLifecycleService;
    private final String webhookSecret;

    public RazorpayWebhookService(
            ObjectMapper objectMapper,
            PaymentRecordRepository paymentRecordRepository,
            RazorpayWebhookEventRepository webhookEventRepository,
            BookingLifecycleService bookingLifecycleService,
            @Value("${festora.razorpay.webhook-secret}") String webhookSecret
    ) {
        this.objectMapper = objectMapper;
        this.paymentRecordRepository = paymentRecordRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.bookingLifecycleService = bookingLifecycleService;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public void processWebhook(
            byte[] rawBody,
            String receivedSignature,
            String eventId
    ) {
        verifySignature(rawBody, receivedSignature);

        if (eventId == null || eventId.isBlank() || eventId.length() > 100) {
            throw new BusinessException("Invalid Razorpay webhook event ID");
        }

        if (webhookEventRepository.existsByEventId(eventId)) {
            return;
        }

        JsonNode payload;

        try {
            payload = objectMapper.readTree(rawBody);
        } catch (Exception exception) {
            throw new BusinessException("Invalid Razorpay webhook payload");
        }

        String eventType = payload.path("event").asText("");

        if (eventType.isBlank()) {
            throw new BusinessException("Razorpay webhook event type is missing");
        }

        webhookEventRepository.save(
                RazorpayWebhookEvent.create(eventId, eventType)
        );

        if ("payment.captured".equals(eventType)) {
            processCapturedPayment(payload);
        }
    }

    private void processCapturedPayment(JsonNode payload) {
        JsonNode paymentNode = payload
                .path("payload")
                .path("payment")
                .path("entity");

        String paymentId = paymentNode.path("id").asText("");
        String orderId = paymentNode.path("order_id").asText("");
        String status = paymentNode.path("status").asText("");
        long amount = paymentNode.path("amount").asLong(-1);
        String currency = paymentNode.path("currency").asText("");

        if (paymentId.isBlank()
                || orderId.isBlank()
                || !"captured".equalsIgnoreCase(status)
                || amount < 0
                || currency.isBlank()) {
            throw new BusinessException("Invalid captured payment details");
        }

        PaymentRecord payment = paymentRecordRepository
                .findByRazorpayOrderId(orderId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Payment order does not exist in Festora"
                        )
                );

        if (!payment.getAmountPaise().equals(amount)
                || !payment.getCurrency().equalsIgnoreCase(currency)) {
            throw new BusinessException(
                    "Captured payment does not match the Festora order"
            );
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (!paymentId.equals(payment.getRazorpayPaymentId())) {
                throw new BusinessException(
                        "A different payment is already linked to this order"
                );
            }
            return;
        }

        payment.markSuccessful(paymentId);

        Booking booking = payment.getBooking();

        if (booking.getStatus() == BookingStatus.PENDING
                && booking.getExpiresAt().isAfter(Instant.now())) {
            bookingLifecycleService.confirmAfterPayment(
                    booking.getBookingReference()
            );
        }
    }

    private void verifySignature(
            byte[] rawBody,
            String receivedSignature
    ) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException(
                    "Razorpay webhook secret is not configured"
            );
        }

        if (receivedSignature == null
                || !receivedSignature.matches("(?i)^[a-f0-9]{64}$")) {
            throw new BusinessException(
                    "Invalid Razorpay webhook signature"
            );
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            byte[] digest = mac.doFinal(rawBody);
            String expectedSignature = HexFormat.of().formatHex(digest);

            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.US_ASCII),
                    receivedSignature.toLowerCase()
                            .getBytes(StandardCharsets.US_ASCII)
            );

            if (!valid) {
                throw new BusinessException(
                        "Razorpay webhook signature verification failed"
                );
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(
                    "Unable to verify Razorpay webhook signature"
            );
        }
    }
}