package com.festora_backend.category.service;

import com.festora_backend.category.dto.CategoryRequest;
import com.festora_backend.category.dto.CategoryResponse;
import com.festora_backend.category.entity.Category;
import com.festora_backend.category.repository.CategoryRepository;
import com.festora_backend.common.exception.BusinessException;
import com.festora_backend.common.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = normalizeName(request.name());

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Category already exists");
        }

        Category category = new Category(
                name,
                normalizeDescription(request.description())
        );

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(
            String search,
            Pageable pageable
    ) {
        Page<Category> categories;

        if (search == null || search.isBlank()) {
            categories = categoryRepository.findAll(pageable);
        } else {
            categories = categoryRepository
                    .findByNameContainingIgnoreCase(
                            search.trim(),
                            pageable
                    );
        }

        return categories.map(this::toResponse);
    }

    @Transactional
    public CategoryResponse update(
            Long id,
            CategoryRequest request
    ) {
        Category category = findCategory(id);
        String name = normalizeName(request.name());

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(
                name,
                id
        )) {
            throw new BusinessException("Category already exists");
        }

        category.update(
                name,
                normalizeDescription(request.description())
        );

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateStatus(
            Long id,
            boolean active
    ) {
        Category category = findCategory(id);

        if (active) {
            category.activate();
        } else {
            category.deactivate();
        }

        return toResponse(categoryRepository.save(category));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", id)
                );
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Category name is required");
        }

        return name.trim();
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank()
                ? null
                : description.trim();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}