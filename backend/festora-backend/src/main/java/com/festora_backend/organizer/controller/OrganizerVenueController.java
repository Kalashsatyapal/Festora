
package com.festora_backend.organizer.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.organizer.dto.AssignedVenueResponse;
import com.festora_backend.organizer.service.OrganizerVenueService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/venues")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerVenueController {

    private final OrganizerVenueService organizerVenueService;

    public OrganizerVenueController(
            OrganizerVenueService organizerVenueService
    ) {
        this.organizerVenueService = organizerVenueService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssignedVenueResponse>>> getAssignedVenues(
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assigned venues retrieved successfully",
                        organizerVenueService.getAssignedVenues(principal.getName())
                )
        );
    }
}
