package com.tetgift.service.impl;

import com.tetgift.dto.request.CategoryRequest;
import com.tetgift.dto.response.CategoryResponse;
import com.tetgift.exception.CategoryNotFoundException;
import com.tetgift.model.Category;
import com.tetgift.repository.jpa.CategoryRepository;
import com.tetgift.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository cateRepository;

    @Override
    public List<CategoryResponse> getAllCategory() {
        return cateRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCate(CategoryRequest cate) {
        Category newCate = Category.builder()
                .name(cate.getName())
                .description(cate.getDescription())
                .isActive(true)
                .build();

        Category savedCate = cateRepository.save(newCate);
        return toResponse(savedCate);
    }

    @Override
    @Transactional
    public CategoryResponse updateCate(Long id, CategoryRequest cate) {
        Category existingCate = cateRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        existingCate.setName(cate.getName());
        existingCate.setDescription(cate.getDescription());

        Category updatedCate = cateRepository.save(existingCate);
        return toResponse(updatedCate);
    }

    @Override
    @Transactional
    public void deleteCate(Long id) {
        Category cate = cateRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        // Soft delete
        cate.setActive(false);
        cateRepository.save(cate);
    }

    @Override
    public CategoryResponse getCateById(Long id) {
        Category cate = cateRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        return toResponse(cate);
    }

    private CategoryResponse toResponse(Category cate) {
        return CategoryResponse.builder()
                .id(cate.getId())
                .name(cate.getName())
                .description(cate.getDescription())
                .isActive(cate.isActive())
                .build();
    }
}
