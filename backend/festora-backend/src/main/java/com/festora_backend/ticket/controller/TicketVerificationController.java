
package com.festora_backend.ticket.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.ticket.dto.TicketVerificationResponse;
import com.festora_backend.ticket.service.TicketVerificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketVerificationController {

    private final TicketVerificationService verificationService;

    public TicketVerificationController(
            TicketVerificationService verificationService
    ) {
        this.verificationService = verificationService;
    }

    @GetMapping("/verify/{code}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TicketVerificationResponse>> verify(
            @PathVariable UUID code,
            Authentication authentication
    ) {
        TicketVerificationResponse response =
                verificationService.verify(
                        code,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Ticket verified successfully. Entry approved.",
                        response
                )
        );
    }
}
