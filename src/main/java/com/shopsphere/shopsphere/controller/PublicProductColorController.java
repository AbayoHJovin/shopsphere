package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.ProductColorResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.ProductColorService;
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

import java.util.List;
import java.util.UUID;

/**
 * Controller for public access to product colors
 * This provides endpoints that don't require authentication
 */
@RestController
@RequestMapping("/api/public/product-colors")
@RequiredArgsConstructor
@Slf4j
public class PublicProductColorController {

    private final ProductColorService productColorService;

    /**
     * Get a product color by ID
     */
    @GetMapping("/{colorId}")
    public ResponseEntity<?> getProductColorById(
            @PathVariable UUID colorId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching product color with ID: {}", colorId);
            ProductColorResponse response = productColorService.getProductColorById(colorId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product color not found", e);
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
    public ResponseEntity<?> getColorsByIds(
            @RequestParam List<UUID> colorIds,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching colors by IDs: {}", colorIds);
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
    public ResponseEntity<?> getAllProductColors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "colorName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching all product colors with pagination");
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
    public ResponseEntity<?> searchProductColors(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Searching product colors with name: {}", name);
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