package com.festora_backend.admin.controller;

import com.festora_backend.admin.dto.RefundRequest;
import com.festora_backend.admin.dto.RefundResponse;
import com.festora_backend.admin.service.AdminRefundService;
import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.admin.dto.RefundAdminResponse;
import com.festora_backend.admin.service.AdminRefundQueryService;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.response.PageResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/refunds")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {
        private final AdminRefundQueryService refundQueryService;
        private final AdminRefundService refundService;

        public AdminRefundController(
                        AdminRefundService refundService,
                        AdminRefundQueryService refundQueryService) {
                this.refundService = refundService;
                this.refundQueryService = refundQueryService;
        }

        @PostMapping("/{bookingReference}")
        public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
                        @PathVariable UUID bookingReference,
                        @Valid @RequestBody RefundRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Refund request processed",
                                                refundService.initiateRefund(
                                                                bookingReference,
                                                                request.reason())));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<RefundAdminResponse>>> getRefunds(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) PaymentStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Refund records retrieved successfully",
                                                refundQueryService.getRefunds(
                                                                search,
                                                                status,
                                                                page,
                                                                size)));
        }

        @GetMapping("/{refundId}")
        public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
                        @PathVariable Long refundId) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Refund details retrieved successfully",
                                                refundService.getRefund(refundId)));
        }
}