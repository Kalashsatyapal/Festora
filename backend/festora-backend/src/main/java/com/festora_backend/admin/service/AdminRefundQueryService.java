package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.RefundAdminResponse;
import com.festora_backend.admin.entity.RefundAudit;
import com.festora_backend.admin.repository.RefundAuditRepository;
import com.festora_backend.common.enums.PaymentStatus;
import com.festora_backend.common.response.PageResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRefundQueryService {

    private final RefundAuditRepository refundRepository;

    public AdminRefundQueryService(RefundAuditRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundAdminResponse> getRefunds(
            String search,
            PaymentStatus status,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        String keyword = search == null || search.isBlank()
                ? null
                : search.trim();

        return PageResponse.from(
                refundRepository.searchAdminRefunds(
                        status,
                        keyword,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, RefundAudit::getCreatedAt)
                        )
                ).map(RefundAdminResponse::from)
        );
    }
}