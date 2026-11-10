package com.festora_backend.venue.service;

import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.organizer.repository.OrganizerVenueRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.venue.dto.VenueAvailabilityResponse;
import com.festora_backend.venue.entity.Venue;
import com.festora_backend.venue.repository.VenueRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VenueCalendarService {

        private static final List<EventStatus> BLOCKING_STATUSES = List.of(EventStatus.DRAFT, EventStatus.PUBLISHED);

        private final VenueRepository venueRepository;
        private final EventRepository eventRepository;
        private final OrganizerVenueRepository assignmentRepository;

        public VenueCalendarService(
                        VenueRepository venueRepository,
                        EventRepository eventRepository,
                        OrganizerVenueRepository assignmentRepository) {
                this.venueRepository = venueRepository;
                this.eventRepository = eventRepository;
                this.assignmentRepository = assignmentRepository;
        }

        public VenueAvailabilityResponse checkAvailability(
                        Long venueId,
                        Instant start,
                        Instant end,
                        Long excludeEventId,
                        User organizer) {
                validateDateRange(start, end);
                getActiveVenue(venueId);
                validateVenueAssignment(venueId, organizer);
                boolean conflict = eventRepository.hasVenueConflict(
                                venueId,
                                start,
                                end,
                                excludeEventId,
                                BLOCKING_STATUSES);
                if (conflict) {
                        return new VenueAvailabilityResponse(
                                        venueId,
                                        false,
                                        start,
                                        end,
                                        "Venue is already booked during the requested time");
                }
                return new VenueAvailabilityResponse(
                                venueId,
                                true,
                                start,
                                end,
                                "Venue is available for the requested time");
        }
        public void assertCanSchedule(
                        Long venueId,
                        Instant start,
                        Instant end,
                        Long excludeEventId,
                        User organizer) {
                VenueAvailabilityResponse availability = checkAvailability(
                                venueId,
                                start,
                                end,
                                excludeEventId,
                                organizer);
                if (!availability.available()) {
                        throw new BusinessException(availability.message());
                }
        }
        private void validateDateRange(Instant start, Instant end) {
                if (start == null || end == null || !end.isAfter(start)) {
                        throw new BusinessException(
                                        "Start and end times must be valid, and end must be after start");
                }
        }
        private Venue getActiveVenue(Long venueId) {
                Venue venue = venueRepository.findById(venueId)
                                .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));
                if (!venue.isActive()) {
                        throw new BusinessException("Venue is inactive");
                }

                return venue;
        }

        private void validateVenueAssignment(
                        Long venueId,
                        User organizer) {
                if (organizer == null
                                || !assignmentRepository.existsByOrganizerIdAndVenueIdAndActiveTrue(
                                                organizer.getId(),
                                                venueId)) {
                        throw new BusinessException(
                                        "You can only use venues assigned to you by the admin");
                }
        }
}