package com.festora_backend.admin.controller;

import com.festora_backend.admin.dto.UserAdminResponse;
import com.festora_backend.admin.service.AdminUserService;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.common.response.PageResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserAdminResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users retrieved successfully",
                        adminUserService.getUsers(search, page, size)
                )
        );
    }

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<UserAdminResponse>> suspendUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User suspended successfully",
                        adminUserService.suspendUser(
                                userId,
                                authentication.getName()
                        )
                )
        );
    }

    @PatchMapping("/{userId}/reactivate")
    public ResponseEntity<ApiResponse<UserAdminResponse>> reactivateUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User reactivated successfully",
                        adminUserService.reactivateUser(userId)
                )
        );
    }
}