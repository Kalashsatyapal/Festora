package com.festora_backend.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryStatusRequest(
        @NotNull
        Boolean active
) {
}