package com.festora_backend.organizer.dto;

public record AttendeeRosterCsvResponse(
        String fileName,
        String contentType,
        String csvContent
) {
}