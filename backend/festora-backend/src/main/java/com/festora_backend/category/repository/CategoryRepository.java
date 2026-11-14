package com.festora_backend.category.repository;

import com.festora_backend.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Category> findByActiveTrue();

    Page<Category> findByActiveTrue(Pageable pageable);

    Page<Category> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}