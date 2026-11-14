package com.festora_backend.category.controller;

import com.festora_backend.category.dto.CategoryRequest;
import com.festora_backend.category.dto.CategoryResponse;
import com.festora_backend.category.dto.CategoryStatusRequest;
import com.festora_backend.category.service.CategoryService;
import com.festora_backend.common.response.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Category created successfully",
                        categoryService.create(request)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories retrieved successfully",
                        categoryService.getCategories(
                                search,
                                PageRequest.of(
                                        page,
                                        size,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                        )
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category updated successfully",
                        categoryService.update(id, request)
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CategoryStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category status updated successfully",
                        categoryService.updateStatus(
                                id,
                                request.active()
                        )
                )
        );
    }
}