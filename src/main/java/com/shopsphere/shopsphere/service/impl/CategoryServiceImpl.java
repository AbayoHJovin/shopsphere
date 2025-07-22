package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.CategoryCreateRequest;
import com.shopsphere.shopsphere.dto.request.CategoryUpdateRequest;
import com.shopsphere.shopsphere.dto.response.CategoryResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Category;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.repository.CategoryRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Check if the category name already exists
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name " + request.getName() + " already exists");
        }

        // If parentId is provided, find the parent category
        Category parentCategory = null;
        if (request.getParentId() != null) {
            parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
        }

        // Create the category
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parentCategory)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapCategoryToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request) {
        log.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Check if the updated name already exists in another category
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name " + request.getName() + " already exists");
        }

        // Handle parent category change
        if (request.getParentId() != null && !request.getParentId().equals(category.getParent() != null ? category.getParent().getCategoryId() : null)) {
            // Check for circular references
            if (request.getParentId().equals(categoryId)) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }

            // Check if the new parent exists
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));

            // Check if the new parent is not a descendant of this category
            if (isDescendant(newParent, category)) {
                throw new IllegalArgumentException("Cannot set a subcategory as parent (circular reference)");
            }

            category.setParent(newParent);
        } else if (request.getParentId() == null && category.getParent() != null) {
            // Remove the parent if parentId is null
            category.setParent(null);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return mapCategoryToResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        log.info("Deleting category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Delete all products associated with this category and its subcategories
        deleteProductsInCategory(category);
        
        // Delete the category (will automatically remove subcategories due to CascadeType.ALL)
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId) {
        log.info("Fetching category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return mapCategoryToResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");

        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getTopLevelCategories() {
        log.info("Fetching top-level categories");

        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapCategoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        log.info("Fetching subcategories of category with ID: {}", parentId);

        // Verify parent exists
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent category not found with ID: " + parentId);
        }

        return categoryRepository.findByParentId(parentId).stream()
                .map(this::mapCategoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySummaryResponse> getCategorySummaries() {
        log.info("Fetching category summaries");

        List<Category> allCategories = categoryRepository.findAll();
        return allCategories.stream()
                .map(category -> CategorySummaryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .productCount(category.getProducts().size())
                        .hasSubcategories(!category.getSubcategories().isEmpty())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Recursively delete all products associated with the category and its subcategories
     */
    private void deleteProductsInCategory(Category category) {
        // Delete products in this category
        for (Product product : new ArrayList<>(category.getProducts())) {
            // Remove from all categories
            product.getCategories().clear();
            // Delete the product
            productRepository.delete(product);
        }

        // Recursively delete products in subcategories
        for (Category subcategory : category.getSubcategories()) {
            deleteProductsInCategory(subcategory);
        }
    }

    /**
     * Check if a category is a descendant of another category
     */
    private boolean isDescendant(Category potentialDescendant, Category ancestor) {
        // If potential descendant has no parent, it can't be a descendant
        if (potentialDescendant.getParent() == null) {
            return false;
        }
        
        // If the parent is the ancestor, then it's a direct descendant
        if (potentialDescendant.getParent().getCategoryId().equals(ancestor.getCategoryId())) {
            return true;
        }
        
        // Recursively check the parent
        return isDescendant(potentialDescendant.getParent(), ancestor);
    }

    private CategoryResponse mapCategoryToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .hasSubcategories(!category.getSubcategories().isEmpty())
                .subcategoryCount(category.getSubcategories().size())
                .productCount(category.getProducts().size())
                .build();
    }
} 