package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.ProductColorRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.dto.response.ProductColorResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.ProductColorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing product colors (Admin/Co-worker operations)
 */
@RestController
@RequestMapping("/api/product-colors")
@RequiredArgsConstructor
@Slf4j
public class ProductColorController {

    private final ProductColorService productColorService;

    /**
     * Create a new product color
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> createProductColor(
            @Valid @RequestBody ProductColorRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating product color: {}", request.getColorName());
            ProductColorResponse response = productColorService.createProductColor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error creating product color", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating product color: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    /**
     * Update an existing product color
     */
    @PutMapping("/{colorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> updateProductColor(
            @PathVariable UUID colorId,
            @Valid @RequestBody ProductColorRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating product color with ID: {}", colorId);
            ProductColorResponse response = productColorService.updateProductColor(colorId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating product color", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating product color: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    /**
     * Delete a product color
     */
    @DeleteMapping("/{colorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> deleteProductColor(
            @PathVariable UUID colorId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting product color with ID: {}", colorId);
            productColorService.deleteProductColor(colorId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Product color deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting product color", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting product color: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    /**
     * Get a product color by ID
     */
    @GetMapping("/{colorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> getProductColorById(
            @PathVariable UUID colorId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching product color with ID: {}", colorId);
            ProductColorResponse response = productColorService.getProductColorById(colorId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching product color", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching product color: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    /**
     * Get colors by their IDs
     */
    @GetMapping("/by-ids")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> getColorsByIds(
            @RequestParam List<UUID> colorIds,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching colors by IDs: {}", colorIds);
            var response = productColorService.getColorsByIds(colorIds);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching product colors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching product colors: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    /**
     * Get all product colors with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> getAllProductColors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "colorName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all product colors with pagination");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductColorResponse> response = productColorService.getAllProductColors(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching product colors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching product colors: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    /**
     * Search for product colors by name
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> searchProductColors(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Searching product colors with name: {}", name);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductColorResponse> response = productColorService.searchProductColors(name, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching product colors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error searching product colors: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
}