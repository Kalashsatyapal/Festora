package com.festora_backend.admin.controller;

import com.festora_backend.admin.dto.OrganizerVerificationResponse;
import com.festora_backend.admin.service.AdminOrganizerService;
import com.festora_backend.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/organizers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizerController {

    private final AdminOrganizerService organizerService;

    public AdminOrganizerController(
            AdminOrganizerService organizerService
    ) {
        this.organizerService = organizerService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OrganizerVerificationResponse>>> getPending() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Pending organizers retrieved successfully",
                        organizerService.getPendingOrganizers()
                )
        );
    }

    @PutMapping("/{organizerId}/approve")
    public ResponseEntity<ApiResponse<OrganizerVerificationResponse>> approve(
            @PathVariable Long organizerId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organizer approved successfully",
                        organizerService.approve(organizerId)
                )
        );
    }

    @PutMapping("/{organizerId}/reject")
    public ResponseEntity<ApiResponse<OrganizerVerificationResponse>> reject(
            @PathVariable Long organizerId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organizer rejected successfully",
                        organizerService.reject(organizerId)
                )
        );
    }
}