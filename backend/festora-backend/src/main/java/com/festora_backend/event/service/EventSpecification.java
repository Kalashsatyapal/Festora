package com.festora_backend.event.service;

import com.festora_backend.event.entity.Event;
import com.festora_backend.common.enums.EventStatus;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> discover(
            Long categoryId,
            Long venueId,
            Instant startFrom,
            Instant startTo,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(
                            root.get("status"),
                            EventStatus.PUBLISHED
                    )
            );

            if (categoryId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category").get("id"),
                                categoryId
                        )
                );
            }

            if (venueId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("venue").get("id"),
                                venueId
                        )
                );
            }

            if (startFrom != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("startAt"),
                                startFrom
                        )
                );
            }

            if (startTo != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("startAt"),
                                startTo
                        )
                );
            }

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + keyword.trim().toLowerCase() + "%"
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }
}