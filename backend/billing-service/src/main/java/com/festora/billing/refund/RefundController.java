package com.festora.billing.refund;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<RefundResponse> refund(
            @Valid @RequestBody RefundRequest request
    ) {
        return ResponseEntity.ok(refundService.process(request));
    }
}