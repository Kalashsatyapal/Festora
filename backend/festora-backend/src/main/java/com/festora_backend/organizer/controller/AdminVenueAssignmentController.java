package com.festora_backend.organizer.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.organizer.dto.VenueAssignmentRequest;
import com.festora_backend.organizer.dto.AssignedVenueResponse;
import com.festora_backend.organizer.service.OrganizerVenueService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/organizer-venues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueAssignmentController {

    private final OrganizerVenueService service;

    public AdminVenueAssignmentController(OrganizerVenueService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AssignedVenueResponse>> assign(
            @Valid @RequestBody VenueAssignmentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Venue assigned successfully",
                        service.assign(
                                request.organizerId(),
                                request.venueId()
                        )
                )
        );
    }

    @DeleteMapping("/{organizerId}/{venueId}")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @PathVariable Long organizerId,
            @PathVariable Long venueId
    ) {
        service.revoke(organizerId, venueId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Venue assignment revoked successfully",
                        null
                )
        );
    }
}
