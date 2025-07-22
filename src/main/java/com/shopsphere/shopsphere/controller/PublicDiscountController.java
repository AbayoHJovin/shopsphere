package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.DiscountResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.DiscountService;
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
 * Controller for public discount operations
 * This provides endpoints that don't require authentication
 */
@RestController
@RequestMapping("/api/public/discounts")
@RequiredArgsConstructor
@Slf4j
public class PublicDiscountController {

    private final DiscountService discountService;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching active discounts with pagination");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DiscountResponse> response = discountService.getActiveDiscounts(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching active discounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching active discounts: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/product/{productId}/current")
    public ResponseEntity<?> getCurrentDiscountsForProduct(
            @PathVariable UUID productId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Fetching current discounts for product with ID: {}", productId);
            List<DiscountResponse> response = discountService.getCurrentDiscountsForProduct(productId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching current discounts for product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching current discounts for product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
}