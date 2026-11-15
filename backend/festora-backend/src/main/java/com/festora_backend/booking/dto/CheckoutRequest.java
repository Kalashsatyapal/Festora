package com.festora_backend.booking.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
public record CheckoutRequest(

        @NotNull(message = "Event ID is required")
        Long eventId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Size(max = 10, message = "You can select at most 10 seats")
        List<@NotBlank @Size(max = 30) String> seatNumbers

) {
}
