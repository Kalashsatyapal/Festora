
package com.festora_backend.event.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.common.response.PageResponse;
import com.festora_backend.event.dto.*;
import com.festora_backend.event.service.EventCancellationService;
import com.festora_backend.event.service.EventService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/events")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerEventController {

        private final EventService eventService;
        private final EventCancellationService cancellationService;

        public OrganizerEventController(
                        EventService eventService,
                        EventCancellationService cancellationService) {
                this.eventService = eventService;
                this.cancellationService = cancellationService;
        }

        @PostMapping
        public ResponseEntity<ApiResponse<EventResponse>> create(
                        @Valid @RequestBody EventCreateRequest request,
                        Authentication authentication) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.success(
                                                "Event created successfully",
                                                eventService.create(
                                                                request,
                                                                authentication.getName())));
        }

        @PutMapping("/{eventId}")
        public ResponseEntity<ApiResponse<EventResponse>> update(
                        @PathVariable Long eventId,
                        @Valid @RequestBody EventUpdateRequest request,
                        Authentication authentication) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Event updated successfully",
                                                eventService.update(
                                                                eventId,
                                                                request,
                                                                authentication.getName())));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> myEvents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Organizer events retrieved successfully",
                                                eventService.organizerEvents(
                                                                authentication.getName(),
                                                                page,
                                                                size)));
        }

        @GetMapping("/{eventId}")
        public ResponseEntity<ApiResponse<EventResponse>> getMyEvent(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Event retrieved successfully",
                                                eventService.getOrganizerEvent(
                                                                eventId,
                                                                authentication.getName())));
        }

        @PostMapping(value = "/{eventId}/images", consumes = "multipart/form-data")
        public ResponseEntity<ApiResponse<EventResponse>> uploadImages(
                        @PathVariable Long eventId,
                        @RequestParam("files") List<MultipartFile> files,
                        Authentication authentication) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Event images uploaded successfully",
                                                eventService.uploadImages(
                                                                eventId,
                                                                files,
                                                                authentication.getName())));
        }

        @DeleteMapping("/{eventId}/images/{imageIndex}")
        public ResponseEntity<ApiResponse<EventResponse>> removeImage(
                        @PathVariable Long eventId,
                        @PathVariable int imageIndex,
                        Authentication authentication) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Event image removed successfully",
                                                eventService.removeImage(
                                                                eventId,
                                                                imageIndex,
                                                                authentication.getName())));
        }

        @PatchMapping("/{eventId}/publish")
        public ResponseEntity<ApiResponse<EventResponse>> publish(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Event published successfully",
                                                eventService.publish(
                                                                eventId,
                                                                authentication.getName())));
        }

        @PatchMapping("/{eventId}/cancel")
        public ResponseEntity<ApiResponse<EventResponse>> cancel(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                String organizerEmail = authentication.getName();

                cancellationService.cancel(eventId, organizerEmail);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Event cancelled successfully",
                                                eventService.getOrganizerEvent(
                                                                eventId,
                                                                organizerEmail)));
        }

        @DeleteMapping("/{eventId}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                eventService.deleteDraft(
                                eventId,
                                authentication.getName());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Draft event deleted successfully",
                                                null));
        }

        private EventResponse eventServiceResponse(
                        com.festora_backend.event.entity.Event event) {
                return eventService.getOrganizerEvent(
                                event.getId(),
                                event.getOrganizer().getEmail());
        }
}
