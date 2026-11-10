package com.festora_backend.organizer.controller;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.organizer.dto.AssignedVenueResponse;
import com.festora_backend.organizer.service.OrganizerVenueService;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizer/venues")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerVenueController {

    private final OrganizerVenueService service;
    private final UserRepository userRepository;

    public OrganizerVenueController(
            OrganizerVenueService service,
            UserRepository userRepository
    ) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssignedVenueResponse>>> myVenues(
            Authentication authentication
    ) {
        var organizer = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assigned venues retrieved successfully",
                        service.getOrganizerVenues(organizer.getId())
                )
        );
    }
}
