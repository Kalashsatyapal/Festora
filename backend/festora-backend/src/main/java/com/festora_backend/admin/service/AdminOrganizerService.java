package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.OrganizerVerificationResponse;
import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminOrganizerService {

    private final UserRepository userRepository;

    public AdminOrganizerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizerVerificationResponse> getPendingOrganizers() {
        return userRepository.findByRoleAndStatus(
                        UserRole.ORGANIZER,
                        AccountStatus.PENDING
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrganizerVerificationResponse approve(Long organizerId) {
        User organizer = getOrganizer(organizerId);

        if (organizer.getStatus() != AccountStatus.PENDING) {
            throw new BusinessException(
                    "Only pending organizers can be approved"
            );
        }

        organizer.activateOrganizer();
        return toResponse(organizer);
    }

    @Transactional
    public OrganizerVerificationResponse reject(Long organizerId) {
        User organizer = getOrganizer(organizerId);

        if (organizer.getStatus() != AccountStatus.PENDING) {
            throw new BusinessException(
                    "Only pending organizers can be rejected"
            );
        }

        organizer.rejectOrganizer();
        return toResponse(organizer);
    }

    private User getOrganizer(Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organizer",
                                organizerId
                        )
                );

        if (organizer.getRole() != UserRole.ORGANIZER) {
            throw new BusinessException(
                    "The selected account is not an organizer"
            );
        }

        return organizer;
    }

    private OrganizerVerificationResponse toResponse(User organizer) {
        return new OrganizerVerificationResponse(
                organizer.getId(),
                organizer.getFullName(),
                organizer.getEmail(),
                organizer.getStatus().name(),
                organizer.isEnabled(),
                organizer.getCreatedAt()
        );
    }
}