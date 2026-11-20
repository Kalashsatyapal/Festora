
package com.festora_backend.payment.controller;

import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.payment.service.RazorpayWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/webhooks")
public class RazorpayWebhookController {

    private final RazorpayWebhookService webhookService;

    public RazorpayWebhookController(
            RazorpayWebhookService webhookService
    ) {
        this.webhookService = webhookService;
    }

    @PostMapping(
            value = "/razorpay",
            consumes = "application/json"
    )
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            ) String signature,
            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false
            ) String eventId
    ) {
        try {
            webhookService.processWebhook(
                    rawBody,
                    signature,
                    eventId
            );

            return ResponseEntity.ok().build();

        } catch (BusinessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid webhook request"
            );

        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Webhook endpoint is not configured"
            );

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Webhook processing failed"
            );
        }
    }
}
