package com.festora_backend.venue.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.venue.dto.AdminVenueResponse;
import com.festora_backend.venue.dto.CreateVenueRequest;
import com.festora_backend.venue.service.AdminVenueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/venues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueController {

    private final AdminVenueService adminVenueService;

    public AdminVenueController(AdminVenueService adminVenueService) {
        this.adminVenueService = adminVenueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminVenueResponse>> create(
            @Valid @RequestBody CreateVenueRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Venue created successfully",
                        adminVenueService.create(request)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminVenueResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Venues retrieved successfully",
                        adminVenueService.getAll()
                )
        );
    }
}