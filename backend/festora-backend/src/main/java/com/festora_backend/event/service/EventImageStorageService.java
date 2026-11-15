package com.festora_backend.event.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EventImageStorageService {

    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private static final Map<String, String> TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path storageDirectory;

    public EventImageStorageService(
            @Value("${festora.storage.event-image-directory}") String directory
    ) throws IOException {
        storageDirectory = Paths.get(directory)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(storageDirectory);
    }

    public List<String> storeAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.size() > 10) {
            throw new IllegalArgumentException("Upload between 1 and 10 images");
        }

        List<String> storedUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                storedUrls.add(store(file));
            }

            return storedUrls;
        } catch (RuntimeException exception) {
            storedUrls.forEach(this::delete);
            throw exception;
        }
    }

    private String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Each image must be 5 MB or smaller");
        }

        String extension = TYPES.get(file.getContentType());

        if (extension == null) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP images are allowed");
        }

        String filename = UUID.randomUUID() + extension;
        Path destination = storageDirectory.resolve(filename).normalize();

        if (!destination.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);

            if (!matchesImage(header, extension)) {
                throw new IllegalArgumentException(
                        "Image content does not match its declared format"
                );
            }

            try (InputStream completeStream = new SequenceInputStream(
                    new ByteArrayInputStream(header),
                    input
            )) {
                Files.copy(completeStream, destination);
            }

            return "/uploads/events/" + filename;

        } catch (IOException exception) {
            throw new IllegalStateException("Event image storage failed", exception);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/events/")) {
            return;
        }

        String filename = imageUrl.substring("/uploads/events/".length());

        if (filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            return;
        }

        Path file = storageDirectory.resolve(filename).normalize();

        if (!file.startsWith(storageDirectory)) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Event image deletion failed", exception);
        }
    }

    private boolean matchesImage(byte[] header, String extension) {
        return switch (extension) {
            case ".jpg" ->
                    header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;

            case ".png" ->
                    header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;

            case ".webp" ->
                    header.length >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P';

            default -> false;
        };
    }
}