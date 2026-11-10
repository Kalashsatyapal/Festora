
package com.festora_backend.venue.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import com.festora_backend.venue.dto.VenueAvailabilityResponse;
import com.festora_backend.venue.service.VenueCalendarService;
import com.festora_backend.common.exception.ResourceNotFoundException;

import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/organizer/venues")
@PreAuthorize("hasRole('ORGANIZER')")
public class VenueAvailabilityController {

    private final VenueCalendarService venueCalendarService;
    private final UserRepository userRepository;

    public VenueAvailabilityController(
            VenueCalendarService venueCalendarService,
            UserRepository userRepository
    ) {
        this.venueCalendarService = venueCalendarService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{venueId}/availability")
    public ResponseEntity<ApiResponse<VenueAvailabilityResponse>>
    checkAvailability(
            @PathVariable Long venueId,

            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant start,

            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant end,

            @RequestParam(required = false)
            Long excludeEventId,

            Authentication authentication
    ) {
        User organizer = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                authentication.getName()
                        )
                );

        VenueAvailabilityResponse result =
                venueCalendarService.checkAvailability(
                        venueId,
                        start,
                        end,
                        excludeEventId,
                        organizer
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result.message(),
                        result
                )
        );
    }
}
