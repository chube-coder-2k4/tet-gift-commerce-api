package com.tetgift.service;

import com.tetgift.dto.request.CategoryRequest;
import com.tetgift.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategory();

    CategoryResponse createCate(CategoryRequest cate);

    CategoryResponse updateCate(Long id, CategoryRequest cate);

    void deleteCate(Long id);

    CategoryResponse getCateById(Long id);
}
