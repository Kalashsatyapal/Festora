
package com.festora_backend.organizer.service;

import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.organizer.dto.AdminVenueAssignmentResponse;
import com.festora_backend.organizer.dto.AssignedVenueResponse;
import com.festora_backend.organizer.dto.VenueAssignmentRequest;
import com.festora_backend.organizer.entity.OrganizerVenue;
import com.festora_backend.organizer.repository.OrganizerVenueRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import com.festora_backend.venue.entity.Venue;
import com.festora_backend.venue.repository.VenueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizerVenueService {

    private final OrganizerVenueRepository organizerVenueRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    public OrganizerVenueService(
            OrganizerVenueRepository organizerVenueRepository,
            UserRepository userRepository,
            VenueRepository venueRepository
    ) {
        this.organizerVenueRepository = organizerVenueRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
    }

    public AdminVenueAssignmentResponse assign(VenueAssignmentRequest request) {
        User organizer = userRepository.findById(request.organizerId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizer", request.organizerId()));

        if (organizer.getRole() != UserRole.ORGANIZER
                || organizer.getStatus() != AccountStatus.ACTIVE
                || !organizer.isEnabled()) {
            throw new BusinessException("Only approved active organizers can receive venue assignments");
        }

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", request.venueId()));

        if (!venue.isActive()) {
            throw new BusinessException("Inactive venues cannot be assigned");
        }

        OrganizerVenue assignment = organizerVenueRepository
                .findByOrganizerIdAndVenueId(organizer.getId(), venue.getId())
                .orElse(null);

        if (assignment != null) {
            if (assignment.isActive()) {
                throw new BusinessException("This venue is already assigned to the organizer");
            }
            assignment.activate();
        } else {
            assignment = new OrganizerVenue(organizer, venue);
        }

        OrganizerVenue saved = organizerVenueRepository.save(assignment);

        return new AdminVenueAssignmentResponse(
                saved.getId(),
                organizer.getId(),
                organizer.getFullName(),
                organizer.getEmail(),
                venue.getId(),
                saved.getAssignedAt()
        );
    }

    public void revoke(Long organizerId, Long venueId) {
        OrganizerVenue assignment = organizerVenueRepository
                .findByOrganizerIdAndVenueId(organizerId, venueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venue assignment",
                        organizerId + "/" + venueId
                ));

        if (!assignment.isActive()) {
            throw new BusinessException("Venue assignment is already inactive");
        }

        assignment.deactivate();
    }

    @Transactional(readOnly = true)
    public List<AssignedVenueResponse> getAssignedVenues(String organizerEmail) {
        User organizer = userRepository.findByEmailIgnoreCase(organizerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", organizerEmail));

        if (organizer.getRole() != UserRole.ORGANIZER
                || organizer.getStatus() != AccountStatus.ACTIVE
                || !organizer.isEnabled()) {
            throw new BusinessException("Active organizer account required");
        }

        return organizerVenueRepository
                .findByOrganizerIdAndActiveTrue(organizer.getId())
                .stream()
                .filter(assignment -> assignment.getVenue().isActive())
                .map(assignment -> new AssignedVenueResponse(
                        assignment.getId(),
                        assignment.getVenue().getId(),
                        assignment.getVenue().getName(),
                        assignment.getVenue().getCity(),
                        assignment.getVenue().getAddress(),
                        assignment.getVenue().getCapacity(),
                        assignment.getAssignedAt()
                ))
                .toList();
    }
}
