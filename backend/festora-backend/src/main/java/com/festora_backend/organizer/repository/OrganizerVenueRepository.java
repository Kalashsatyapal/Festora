
package com.festora_backend.organizer.repository;

import com.festora_backend.organizer.entity.OrganizerVenue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizerVenueRepository
        extends JpaRepository<OrganizerVenue, Long> {

    List<OrganizerVenue> findByOrganizerIdAndActiveTrue(Long organizerId);

    Optional<OrganizerVenue> findByOrganizerIdAndVenueId(
            Long organizerId,
            Long venueId
    );

    boolean existsByOrganizerIdAndVenueIdAndActiveTrue(
            Long organizerId,
            Long venueId
    );
}
