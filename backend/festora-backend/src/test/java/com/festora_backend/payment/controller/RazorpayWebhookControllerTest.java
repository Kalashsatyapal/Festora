
package com.festora_backend.payment.controller;

import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.payment.service.RazorpayWebhookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RazorpayWebhookControllerTest {

    @Mock
    private RazorpayWebhookService webhookService;

    @InjectMocks
    private RazorpayWebhookController controller;

    @Test
    void validWebhookShouldReturnOk() {
        byte[] body = "{}".getBytes();
        String signature = "valid-signature";
        String eventId = "event-123";

        ResponseEntity<Void> response = controller.receiveWebhook(
                body,
                signature,
                eventId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(webhookService).processWebhook(
                body,
                signature,
                eventId
        );
    }

    @Test
    void invalidWebhookShouldReturnBadRequest() {
        doThrow(new BusinessException("Invalid signature"))
                .when(webhookService)
                .processWebhook(
                        org.mockito.ArgumentMatchers.any(byte[].class),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.receiveWebhook(
                        "{}".getBytes(),
                        "invalid",
                        "event-123"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    @Test
    void missingWebhookConfigurationShouldReturnServerError() {
        doThrow(new IllegalStateException("Missing configuration"))
                .when(webhookService)
                .processWebhook(
                        org.mockito.ArgumentMatchers.any(byte[].class),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.receiveWebhook(
                        "{}".getBytes(),
                        "signature",
                        "event-123"
                )
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );
    }

    @Test
    void unexpectedProcessingFailureShouldReturnServerError() {
        doThrow(new RuntimeException("Database unavailable"))
                .when(webhookService)
                .processWebhook(
                        org.mockito.ArgumentMatchers.any(byte[].class),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.receiveWebhook(
                        "{}".getBytes(),
                        "signature",
                        "event-123"
                )
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );
    }
}
