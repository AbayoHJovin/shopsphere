package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.CategoryCreateRequest;
import com.shopsphere.shopsphere.dto.request.CategoryUpdateRequest;
import com.shopsphere.shopsphere.dto.response.CategoryResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new category: {}", request.getName());
            CategoryResponse response = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.error("Parent category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating category with ID: {}", categoryId);
            CategoryResponse response = categoryService.updateCategory(categoryId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable UUID categoryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting category with ID: {}", categoryId);
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Category deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all categories");
            List<CategoryResponse> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/top-level")
    public ResponseEntity<?> getTopLevelCategories(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching top-level categories");
            List<CategoryResponse> categories = categoryService.getTopLevelCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error fetching top-level categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<?> getSubcategories(
            @PathVariable UUID categoryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching subcategories of category with ID: {}", categoryId);
            List<CategoryResponse> subcategories = categoryService.getSubcategories(categoryId);
            return ResponseEntity.ok(subcategories);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching subcategories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(
            @PathVariable UUID categoryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching category with ID: {}", categoryId);
            CategoryResponse category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(category);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/summaries")
    public ResponseEntity<?> getCategorySummaries(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching category summaries");
            List<CategorySummaryResponse> summaries = categoryService.getCategorySummaries();
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("Error fetching category summaries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
} 