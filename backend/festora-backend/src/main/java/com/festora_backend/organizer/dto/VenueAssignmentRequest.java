
package com.festora_backend.organizer.dto;

import jakarta.validation.constraints.NotNull;

public record VenueAssignmentRequest(
        @NotNull Long organizerId,
        @NotNull Long venueId
) {
}
