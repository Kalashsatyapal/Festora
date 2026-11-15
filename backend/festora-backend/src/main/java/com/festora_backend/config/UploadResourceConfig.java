package com.festora_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final String avatarDirectory;
    private final String eventImageDirectory;

    public UploadResourceConfig(
            @Value("${festora.storage.avatar-directory}") String avatarDirectory,
            @Value("${festora.storage.event-image-directory}") String eventImageDirectory
    ) {
        this.avatarDirectory = avatarDirectory;
        this.eventImageDirectory = eventImageDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(getLocation(avatarDirectory));

        registry.addResourceHandler("/uploads/events/**")
                .addResourceLocations(getLocation(eventImageDirectory));
    }

    private String getLocation(String directory) {
        String location = Paths.get(directory)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        return location.endsWith("/") ? location : location + "/";
    }
}