
package com.festora_backend.organizer.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.organizer.dto.AdminVenueAssignmentResponse;
import com.festora_backend.organizer.dto.VenueAssignmentRequest;
import com.festora_backend.organizer.service.OrganizerVenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/organizer-venues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueAssignmentController {

    private final OrganizerVenueService organizerVenueService;

    public AdminVenueAssignmentController(
            OrganizerVenueService organizerVenueService
    ) {
        this.organizerVenueService = organizerVenueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminVenueAssignmentResponse>> assign(
            @Valid @RequestBody VenueAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Venue assigned successfully",
                        organizerVenueService.assign(request)
                )
        );
    }

    @DeleteMapping("/{organizerId}/{venueId}")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @PathVariable Long organizerId,
            @PathVariable Long venueId
    ) {
        organizerVenueService.revoke(organizerId, venueId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Venue assignment revoked successfully",
                        null
                )
        );
    }
}
