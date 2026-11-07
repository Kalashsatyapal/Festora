package com.festora_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class AvatarResourceConfig implements WebMvcConfigurer {

    private final String avatarDirectory;

    public AvatarResourceConfig(
            @Value("${festora.storage.avatar-directory}")
            String avatarDirectory
    ) {
        this.avatarDirectory = Paths.get(avatarDirectory)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarDirectory);
    }
}