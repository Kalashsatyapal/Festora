package com.festora_backend.organizer.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.organizer.dto.AttendeeRosterCsvResponse;
import com.festora_backend.organizer.dto.OrganizerRevenueResponse;
import com.festora_backend.organizer.service.OrganizerEventAnalyticsService;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController 
@RequestMapping("/api/organizer/events/{eventId}")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerEventAnalyticsController {

    private final OrganizerEventAnalyticsService analyticsService;
    private final UserRepository userRepository;

    public OrganizerEventAnalyticsController(
            OrganizerEventAnalyticsService analyticsService,
            UserRepository userRepository
    ) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/attendees/export")
    public ResponseEntity<ApiResponse<AttendeeRosterCsvResponse>> exportAttendees(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        var organizer = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        AttendeeRosterCsvResponse response =
                analyticsService.exportAttendees(
                        eventId,
                        organizer.getId()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendee roster exported successfully",
                        response
                )
        );
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<OrganizerRevenueResponse>> getEventRevenue(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        var organizer = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        OrganizerRevenueResponse response =
                analyticsService.getEventRevenue(
                        eventId,
                        organizer.getId()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Event revenue retrieved successfully",
                        response
                )
        );
    }
}