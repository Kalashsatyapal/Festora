package com.festora_backend.user.entity;

import com.festora_backend.common.enums.AccountStatus;
import com.festora_backend.common.enums.UserRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role_status", columnList = "role,status")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public static User create(
            String fullName,
            String email,
            String passwordHash,
            UserRole role
    ) {
        User user = new User();
        user.fullName = fullName;
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = role;
        user.status = role == UserRole.ORGANIZER
                ? AccountStatus.PENDING
                : AccountStatus.ACTIVE;
        user.enabled = role != UserRole.ORGANIZER;
        return user;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void activateOrganizer() {
        if (role != UserRole.ORGANIZER) {
            throw new IllegalStateException(
                    "Only organizer accounts can be activated"
            );
        }

        status = AccountStatus.ACTIVE;
        enabled = true;
    }

    public void rejectOrganizer() {
        if (role != UserRole.ORGANIZER) {
            throw new IllegalStateException(
                    "Only organizer accounts can be rejected"
            );
        }

        status = AccountStatus.REJECTED;
        enabled = false;
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}