package com.festora_backend.venue.service;

import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.venue.dto.AdminVenueResponse;
import com.festora_backend.venue.dto.CreateVenueRequest;
import com.festora_backend.venue.entity.Venue;
import com.festora_backend.venue.repository.VenueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminVenueService {

    private final VenueRepository venueRepository;

    public AdminVenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public AdminVenueResponse create(CreateVenueRequest request) {
        String name = request.name().trim();
        String city = request.city().trim();
        String address = request.address().trim();

        if (venueRepository.existsByNameIgnoreCaseAndCityIgnoreCase(name, city)) {
            throw new BusinessException(
                    "A venue with this name already exists in this city"
            );
        }

        Venue venue = new Venue(
                name,
                city,
                address,
                request.capacity()
        );

        return toResponse(venueRepository.save(venue));
    }

    @Transactional(readOnly = true)
    public List<AdminVenueResponse> getAll() {
        return venueRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AdminVenueResponse toResponse(Venue venue) {
        return new AdminVenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                venue.getAddress(),
                venue.getCapacity(),
                venue.isActive()
        );
    }
}