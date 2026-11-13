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
    private final String refundPath;
    private final String internalApiKey;

    public BillingClient(
            RestClient.Builder builder,
            @Value("${festora.billing.base-url}") String baseUrl,
            @Value("${festora.billing.refund-path:/api/refunds}") String refundPath,
            @Value("${festora.billing.internal-api-key}") String internalApiKey
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.refundPath = refundPath;
        this.internalApiKey = internalApiKey;
    }

    public BillingRefundResponse refund(BillingRefundRequest request) {
        return restClient.post()
                .uri(refundPath)
                .header("X-Internal-API-Key", internalApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BillingRefundResponse.class);
    }
}