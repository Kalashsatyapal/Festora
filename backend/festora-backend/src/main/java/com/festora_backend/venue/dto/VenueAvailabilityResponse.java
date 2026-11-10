
package com.festora_backend.venue.dto;

import java.time.Instant;

public record VenueAvailabilityResponse(
        Long venueId,
        boolean available,
        Instant requestedStart,
        Instant requestedEnd,
        String message
) {
}
