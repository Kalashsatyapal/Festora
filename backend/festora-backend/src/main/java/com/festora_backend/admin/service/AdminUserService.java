package com.festora_backend.admin.service;

import com.festora_backend.admin.dto.UserAdminResponse;
import com.festora_backend.common.enums.UserRole;
import com.festora_backend.common.exception.ResourceNotFoundException;
import com.festora_backend.common.response.PageResponse;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> getUsers(
            String search,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, User::getCreatedAt)
        );

        Page<User> users;

        if (search == null || search.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            String keyword = search.trim();
            users = userRepository
                    .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            keyword,
                            keyword,
                            pageable
                    );
        }

        return PageResponse.from(users.map(UserAdminResponse::from));
    }

    @Transactional
    public UserAdminResponse suspendUser(Long userId, String adminEmail) {
        User user = findUser(userId);

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalStateException("You cannot suspend your own account");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Admin accounts cannot be suspended");
        }

        user.suspend();

        return UserAdminResponse.from(user);
    }

    @Transactional
    public UserAdminResponse reactivateUser(Long userId) {
        User user = findUser(userId);
        user.reactivate();

        return UserAdminResponse.from(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}