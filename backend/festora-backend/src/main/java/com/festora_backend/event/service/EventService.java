
package com.festora_backend.event.service;

import com.festora_backend.category.entity.Category;
import com.festora_backend.category.repository.CategoryRepository;
import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.EventStatus;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.common.response.PageResponse;
import com.festora_backend.event.dto.*;
import com.festora_backend.event.entity.Event;
import com.festora_backend.event.repository.EventRepository;
import com.festora_backend.organizer.repository.OrganizerVenueRepository;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import com.festora_backend.venue.entity.Venue;
import com.festora_backend.venue.repository.VenueRepository;
import com.festora_backend.ticket.service.TicketInventoryService;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class EventService {

    private final TicketInventoryService ticketInventoryService;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final OrganizerVenueRepository organizerVenueRepository;
    private final EventImageStorageService eventImageStorageService;

    public EventService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository,
            VenueRepository venueRepository,
            UserRepository userRepository,
            OrganizerVenueRepository organizerVenueRepository,
            TicketInventoryService ticketInventoryService,
            EventImageStorageService eventImageStorageService
    ) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.organizerVenueRepository = organizerVenueRepository;
        this.ticketInventoryService = ticketInventoryService;
        this.eventImageStorageService = eventImageStorageService;
    }

    public EventResponse create(EventCreateRequest request, String organizerEmail) {
        User organizer = getOrganizer(organizerEmail);
        Category category = getActiveCategory(request.categoryId());
        Venue venue = getActiveVenue(request.venueId());

        validateVenueAssignment(organizer, venue);
        validateDates(request.startAt(), request.endAt());

        Event event = Event.create(
                request.title(),
                request.description(),
                category,
                venue,
                request.startAt(),
                request.endAt(),
                request.totalCapacity(),
                request.ticketPrice(),
                organizer
        );

        return toResponse(eventRepository.save(event));
    }

    public EventResponse update(
            Long eventId,
            EventUpdateRequest request,
            String organizerEmail
    ) {
        Event event = getOwnedEvent(eventId, organizerEmail);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException("Only draft events can be edited");
        }

        Category category = getActiveCategory(request.categoryId());
        Venue venue = getActiveVenue(request.venueId());

        validateVenueAssignment(event.getOrganizer(), venue);
        validateDates(request.startAt(), request.endAt());

        event.updateDetails(
                request.title(),
                request.description(),
                category,
                venue,
                request.startAt(),
                request.endAt(),
                request.totalCapacity(),
                request.ticketPrice()
        );

        return toResponse(event);
    }

    public EventResponse uploadImages(
            Long eventId,
            List<MultipartFile> files,
            String organizerEmail
    ) {
        Event event = getOwnedEvent(eventId, organizerEmail);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException("Images can only be changed on draft events");
        }

        if (files == null || files.isEmpty()
                || event.getImageUrls().size() + files.size() > 10) {
            throw new BusinessException("An event can have at most 10 images");
        }

        List<String> imageUrls = eventImageStorageService.storeAll(files);
        event.addImageUrls(imageUrls);

        return toResponse(event);
    }

    public EventResponse removeImage(
            Long eventId,
            int imageIndex,
            String organizerEmail
    ) {
        Event event = getOwnedEvent(eventId, organizerEmail);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException("Images can only be changed on draft events");
        }

        String imageUrl = event.removeImage(imageIndex);
        eventImageStorageService.delete(imageUrl);

        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public EventResponse getPublicEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Published event", eventId)
                );

        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public EventResponse getOrganizerEvent(Long eventId, String organizerEmail) {
        return toResponse(getOwnedEvent(eventId, organizerEmail));
    }

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> discover(
            Long categoryId,
            Long venueId,
            Instant startFrom,
            Instant startTo,
            String keyword,
            int page,
            int size
    ) {
        validateSearchDates(startFrom, startTo);

        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Invalid pagination parameters");
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Event::getStartAt).ascending()
        );

        Page<EventResponse> result = eventRepository.findAll(
                EventSpecification.discover(
                        categoryId,
                        venueId,
                        startFrom,
                        startTo,
                        keyword
                ),
                pageable
        ).map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> organizerEvents(
            String organizerEmail,
            int page,
            int size
    ) {
        User organizer = getOrganizer(organizerEmail);

        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Invalid pagination parameters");
        }

        Page<EventResponse> result = eventRepository.findAll(
                (root, query, cb) ->
                        cb.equal(root.get("organizer").get("id"), organizer.getId()),
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Event::getCreatedAt).descending()
                )
        ).map(this::toResponse);

        return PageResponse.from(result);
    }

    public EventResponse publish(Long eventId, String organizerEmail) {
        Event event = getOwnedEvent(eventId, organizerEmail);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException("Only draft events can be published");
        }

        if (event.getCategory() == null
                || !event.getCategory().isActive()
                || !event.getVenue().isActive()) {
            throw new BusinessException("Event category or venue is inactive");
        }

        if (!event.getOrganizer().isEnabled()
                || event.getOrganizer().getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Organizer account is not active");
        }

        if (!event.getStartAt().isAfter(Instant.now())) {
            throw new BusinessException("Event start time must be in the future");
        }

        validateVenueAssignment(event.getOrganizer(), event.getVenue());
        validateVenueSchedule(event);

        ticketInventoryService.initializeForEvent(event);
        event.publish();

        return toResponse(event);
    }

    public void deleteDraft(Long eventId, String organizerEmail) {
        Event event = getOwnedEvent(eventId, organizerEmail);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException("Only draft events can be deleted");
        }

        eventRepository.delete(event);
    }

    public EventResponse cancel(Long eventId, String organizerEmail) {
        Event event = getOwnedEvent(eventId, organizerEmail);
        event.cancel();
        return toResponse(event);
    }

    private User getOrganizer(String email) {
        User organizer = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        if (organizer.getRole() != UserRole.ORGANIZER
                || !organizer.isEnabled()
                || organizer.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Active organizer account required");
        }

        return organizer;
    }

    private Event getOwnedEvent(Long eventId, String email) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (!event.getOrganizer().getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException("You do not own this event");
        }

        return event;
    }

    private Category getActiveCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.isActive()) {
            throw new BusinessException("Selected category is inactive");
        }

        return category;
    }

    private Venue getActiveVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));

        if (!venue.isActive()) {
            throw new BusinessException("Selected venue is inactive");
        }

        return venue;
    }

    private void validateVenueAssignment(User organizer, Venue venue) {
        if (!organizerVenueRepository.existsByOrganizerIdAndVenueIdAndActiveTrue(
                organizer.getId(),
                venue.getId()
        )) {
            throw new BusinessException(
                    "This venue has not been assigned to your organizer account"
            );
        }
    }

    private void validateVenueSchedule(Event event) {
        boolean conflict = eventRepository.hasVenueConflict(
                event.getVenue().getId(),
                event.getStartAt(),
                event.getEndAt(),
                event.getId(),
                List.of(EventStatus.PUBLISHED)
        );

        if (conflict) {
            throw new BusinessException(
                    "This venue already has a published event during the selected time"
            );
        }
    }

    private void validateDates(Instant start, Instant end) {
        if (start == null || end == null
                || !end.isAfter(start)
                || !start.isAfter(Instant.now())) {
            throw new BusinessException(
                    "Event must have valid future start and end dates"
            );
        }
    }

    private void validateSearchDates(Instant from, Instant to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new BusinessException(
                    "End date cannot be before start date"
            );
        }
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory().getId(),
                event.getCategory().getName(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getVenue().getCity(),
                event.getVenue().getAddress(),
                event.getStartAt(),
                event.getEndAt(),
                event.getTotalCapacity(),
                event.getTicketPrice(),
                event.getStatus(),
                event.getOrganizer().getId(),
                event.getOrganizer().getFullName(),
                event.getCreatedAt(),
                event.getImageUrls()
        );
    }
}
