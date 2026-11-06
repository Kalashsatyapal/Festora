package com.festora_backend.venue.repository;

import com.festora_backend.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository
        extends JpaRepository<Venue, Long> {

    List<Venue> findByCityIgnoreCaseAndActiveTrue(String city);

    List<Venue> findByActiveTrue();

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(
            String name,
            String city
    );
}