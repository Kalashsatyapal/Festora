
package com.festora_backend.ticket.controller;

import com.festora_backend.common.response.ApiResponse;
import com.festora_backend.ticket.dto.SeatAvailabilityResponse;
import com.festora_backend.ticket.service.TicketInventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventSeatController {

    private final TicketInventoryService inventoryService;

    public EventSeatController(TicketInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<ApiResponse<List<SeatAvailabilityResponse>>> getSeats(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Event seat availability retrieved successfully",
                        inventoryService.getAvailability(eventId)
                )
        );
    }
}
