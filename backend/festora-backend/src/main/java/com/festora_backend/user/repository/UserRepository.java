package com.festora_backend.user.repository;

import com.festora_backend.user.entity.User;
import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleAndStatus(
            UserRole role,
            AccountStatus status
    );

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email,
            Pageable pageable
    );

    long countByRole(UserRole role);

    long countByStatus(AccountStatus status);
}