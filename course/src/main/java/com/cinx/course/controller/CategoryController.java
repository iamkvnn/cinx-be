package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateCategoryRequest;
import com.cinx.course.dto.request.UpdateCategoryRequest;
import com.cinx.course.dto.response.CategoryResponse;
import com.cinx.course.service.category.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", categoryService.getAllCategories())
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        categoryService.createCategory(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateCategory(@PathVariable String id, @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
