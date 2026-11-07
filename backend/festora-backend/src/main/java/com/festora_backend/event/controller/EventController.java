package com.festora_backend.event.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.common.response.PageResponse;
import com.festora_backend.event.dto.EventResponse;
import com.festora_backend.event.service.EventService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> discover(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Events retrieved successfully",
                        eventService.discover(
                                categoryId,
                                venueId,
                                startFrom,
                                startTo,
                                keyword,
                                page,
                                size
                        )
                )
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Event retrieved successfully",
                        eventService.getPublicEvent(eventId)
                )
        );
    }
}