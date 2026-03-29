package com.cinx.course.service.category;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateCategoryRequest;
import com.cinx.course.dto.request.UpdateCategoryRequest;
import com.cinx.course.dto.response.CategoryResponse;
import com.cinx.course.mapper.CategoryMapper;
import com.cinx.course.model.Category;
import com.cinx.course.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private Category getCategoryEntityById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        return categoryMapper.toDto(getCategoryEntityById(id));
    }

    @Override
    public void createCategory(CreateCategoryRequest request) {
        categoryRepository.save(categoryMapper.toModel(request));
    }

    @Override
    public void updateCategory(String id, UpdateCategoryRequest request) {
        Category category = getCategoryEntityById(id);
        categoryMapper.partialUpdate(category, request);
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(String id) {
        Category category = getCategoryEntityById(id);
        categoryRepository.delete(category);
    }
}
