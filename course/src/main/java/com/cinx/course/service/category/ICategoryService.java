package com.cinx.course.service.category;

import com.cinx.course.dto.request.CreateCategoryRequest;
import com.cinx.course.dto.request.UpdateCategoryRequest;
import com.cinx.course.dto.response.CategoryResponse;

import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(String id);
    void createCategory(CreateCategoryRequest request);
    void updateCategory(String id, UpdateCategoryRequest request);
    void deleteCategory(String id);
}
