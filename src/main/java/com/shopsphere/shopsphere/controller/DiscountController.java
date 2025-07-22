package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.DiscountCreateRequest;
import com.shopsphere.shopsphere.dto.request.DiscountUpdateRequest;
import com.shopsphere.shopsphere.dto.response.DiscountResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.DiscountService;
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

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Slf4j
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDiscount(
            @Valid @RequestBody DiscountCreateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new discount: {}", request.getName());
            DiscountResponse response = discountService.createDiscount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating discount: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PutMapping("/{discountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDiscount(
            @PathVariable UUID discountId,
            @Valid @RequestBody DiscountUpdateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating discount with ID: {}", discountId);
            DiscountResponse response = discountService.updateDiscount(discountId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Discount not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating discount: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/{discountId}")
    public ResponseEntity<?> getDiscountById(
            @PathVariable UUID discountId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching discount with ID: {}", discountId);
            DiscountResponse response = discountService.getDiscountById(discountId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Discount not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching discount: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all discounts with pagination");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DiscountResponse> response = discountService.getAllDiscounts(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching discounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching discounts: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching active discounts with pagination");
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

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getDiscountsByProduct(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching discounts for product with ID: {}", productId);
            Pageable pageable = PageRequest.of(page, size);
            Page<DiscountResponse> response = discountService.getDiscountsByProduct(productId, pageable);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching discounts by product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching discounts by product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/product/{productId}/current")
    public ResponseEntity<?> getCurrentDiscountsForProduct(
            @PathVariable UUID productId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching current discounts for product with ID: {}", productId);
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

    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDiscount(
            @PathVariable UUID discountId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting discount with ID: {}", discountId);
            discountService.deleteDiscount(discountId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Discount deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Discount not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting discount: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PatchMapping("/{discountId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateDiscount(
            @PathVariable UUID discountId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Activating discount with ID: {}", discountId);
            DiscountResponse response = discountService.activateDiscount(discountId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Discount not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error activating discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error activating discount: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PatchMapping("/{discountId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateDiscount(
            @PathVariable UUID discountId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deactivating discount with ID: {}", discountId);
            DiscountResponse response = discountService.deactivateDiscount(discountId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Discount not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deactivating discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deactivating discount: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
}