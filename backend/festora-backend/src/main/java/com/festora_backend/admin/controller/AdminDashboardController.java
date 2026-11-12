package com.festora_backend.admin.controller;

import com.festora_backend.admin.dto.AdminDashboardResponse;
import com.festora_backend.admin.service.AdminDashboardService;
import com.festora_backend.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(
            AdminDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin dashboard retrieved successfully",
                        dashboardService.getDashboard()
                )
        );
    }
}