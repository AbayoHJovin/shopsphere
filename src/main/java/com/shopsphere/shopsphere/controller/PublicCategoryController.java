package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.CategoryResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for public category operations
 * This provides endpoints that don't require authentication
 */
@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories(HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching all categories");
            List<CategoryResponse> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/top-level")
    public ResponseEntity<?> getTopLevelCategories(HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching top-level categories");
            List<CategoryResponse> categories = categoryService.getTopLevelCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error fetching top-level categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(
            @PathVariable UUID categoryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching category with ID: {}", categoryId);
            CategoryResponse category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(category);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<?> getSubcategories(
            @PathVariable UUID categoryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching subcategories of category with ID: {}", categoryId);
            List<CategoryResponse> subcategories = categoryService.getSubcategories(categoryId);
            return ResponseEntity.ok(subcategories);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching subcategories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
} 