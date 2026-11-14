package com.festora_backend.category.controller;

import com.festora_backend.category.dto.CategoryResponse;
import com.festora_backend.category.service.CategoryService;
import com.festora_backend.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories retrieved successfully",
                        categoryService.getActiveCategories()
                )
        );
    }
}