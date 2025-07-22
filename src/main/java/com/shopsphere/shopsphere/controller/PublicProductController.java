package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.ProductSearchFilterRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.ProductResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for public product operations
 * This provides endpoints that don't require authentication
 */
@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
@Slf4j
public class PublicProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable UUID productId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching product with ID: {}", productId);
            ProductResponse response = productService.getProductById(productId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, 
                            e.getMessage(), 
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Error fetching product: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching all products with pagination");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductResponse> response = productService.getAllProducts(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Error fetching products: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Searching products with query: {}", query);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> response = productService.searchProducts(query, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Error searching products: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }
    
    @PostMapping("/advanced-search")
    public ResponseEntity<?> advancedSearchProducts(
            @RequestBody ProductSearchFilterRequest filter,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Performing advanced search with filters: {}", filter);
            Page<ProductResponse> response = productService.advancedSearchProducts(filter);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during advanced search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Error during advanced search: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching products by category ID: {}", categoryId);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> response = productService.getProductsByCategory(categoryId, pageable);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, 
                            e.getMessage(), 
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching products by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Error fetching products by category: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }
}