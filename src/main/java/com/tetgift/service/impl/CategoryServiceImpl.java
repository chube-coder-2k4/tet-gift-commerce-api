package com.tetgift.service.impl;

import com.tetgift.dto.request.CategoryRequest;
import com.tetgift.dto.response.CategoryResponse;
import com.tetgift.model.Category;
import com.tetgift.repository.jpa.CategoryRepository;
import com.tetgift.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository cateRepository;

    @Override
    public List<CategoryResponse> getAllCategory() {
        List<Category> cates = cateRepository.findAll();
        return cates.stream().map(cate -> CategoryResponse.builder()
                .id(cate.getId())
                .name(cate.getName())
                .description(cate.getDescription())
                .build()).toList();
    }

    @Override
    public CategoryResponse createCate(CategoryRequest cate) {
        Category newCate = Category.builder()
                .name(cate.getName())
                .description(cate.getDescription())
                .build();

        Category savedCate = cateRepository.save(newCate);
        return CategoryResponse.builder()
                .id(savedCate.getId())
                .name(savedCate.getName())
                .description(savedCate.getDescription())
                .build();
    }

    @Override
    public CategoryResponse updateCate(Long id, CategoryRequest cate) {
        Category existingCate = cateRepository.findById(id).orElseThrow(() -> new RuntimeException("Cate not found"));
        existingCate.setName(cate.getName());
        existingCate.setDescription(cate.getDescription());
        Category updatedCate = cateRepository.save(existingCate);
        return CategoryResponse.builder()
                .id(updatedCate.getId())
                .name(updatedCate.getName())
                .description(updatedCate.getDescription())
                .build();
    }

    @Override
    public void deleteCate(Long id) {
        cateRepository.deleteById(id);

    }

    @Override
    public CategoryResponse getCateById(Long id) {
        Category cate = cateRepository.findById(id).orElseThrow(() -> new RuntimeException("Cate not found"));
        return CategoryResponse.builder()
                .id(cate.getId())
                .name(cate.getName())
                .description(cate.getDescription())
                .build();
    }
}
