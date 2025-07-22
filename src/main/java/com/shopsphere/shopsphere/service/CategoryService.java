package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.CategoryCreateRequest;
import com.shopsphere.shopsphere.dto.request.CategoryUpdateRequest;
import com.shopsphere.shopsphere.dto.response.CategoryResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    
    CategoryResponse createCategory(CategoryCreateRequest request);
    
    CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request);
    
    void deleteCategory(UUID categoryId);
    
    CategoryResponse getCategoryById(UUID categoryId);
    
    List<CategoryResponse> getAllCategories();
    
    List<CategoryResponse> getTopLevelCategories();
    
    List<CategoryResponse> getSubcategories(UUID parentId);
    
    List<CategorySummaryResponse> getCategorySummaries();
} 