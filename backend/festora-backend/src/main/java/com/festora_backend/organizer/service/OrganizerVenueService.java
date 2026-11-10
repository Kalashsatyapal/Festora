package com.festora_backend.organizer.service;

import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.organizer.dto.AssignedVenueResponse;
import com.festora_backend.organizer.entity.OrganizerVenue;
import com.festora_backend.organizer.repository.OrganizerVenueRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import com.festora_backend.venue.entity.Venue;
import com.festora_backend.venue.repository.VenueRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizerVenueService {

    private final OrganizerVenueRepository assignmentRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    public OrganizerVenueService(
            OrganizerVenueRepository assignmentRepository,
            UserRepository userRepository,
            VenueRepository venueRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
    }

    public AssignedVenueResponse assign(Long organizerId, Long venueId) {

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer", organizerId));

        if (organizer.getRole() != UserRole.ORGANIZER
                || organizer.getStatus() != AccountStatus.ACTIVE
                || !organizer.isEnabled()) {
            throw new BusinessException(
                    "Only approved and active organizers can receive venue assignments");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));

        if (!venue.isActive()) {
            throw new BusinessException("Venue is inactive");
        }

        OrganizerVenue assignment = assignmentRepository
                .findByOrganizerIdAndVenueId(organizerId, venueId)
                .map(existing -> {
                    existing.activate();
                    return existing;
                })
                .orElseGet(() -> new OrganizerVenue(organizer, venue));

        return toResponse(assignmentRepository.save(assignment));
    }

    public void revoke(Long organizerId, Long venueId) {

        OrganizerVenue assignment = assignmentRepository
                .findByOrganizerIdAndVenueId(organizerId, venueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venue assignment",
                        organizerId + ":" + venueId));

        assignment.deactivate();
    }

    @Transactional(readOnly = true)
    public List<AssignedVenueResponse> getOrganizerVenues(Long organizerId) {

        return assignmentRepository
                .findByOrganizerIdAndActiveTrue(organizerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AssignedVenueResponse toResponse(OrganizerVenue assignment) {

        Venue venue = assignment.getVenue();

        return new AssignedVenueResponse(
                assignment.getId(),
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                venue.getAddress(),
                venue.getCapacity(),
                assignment.getAssignedAt());
    }
}
