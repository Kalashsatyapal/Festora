package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.BillingRefundRequest;
import com.festora_backend.admin.dto.BillingRefundResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BillingClient {

    private final RestClient restClient;

    public BillingClient(
            RestClient.Builder builder,
            @Value("${festora.billing.base-url}") String baseUrl,
            @Value("${festora.billing.refund-path:/api/refunds}") String refundPath
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
        this.refundPath = refundPath;
    }

    private final String refundPath;

    public BillingRefundResponse refund(BillingRefundRequest request) {
        return restClient.post()
                .uri(refundPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BillingRefundResponse.class);
    }
}