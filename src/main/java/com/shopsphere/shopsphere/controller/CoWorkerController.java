package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.dto.response.CoWorkerDashboardResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.ProductSummaryResponse;
import com.shopsphere.shopsphere.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for CO_WORKER specific operations
 * This provides a cleaner separation between ADMIN and CO_WORKER
 * functionalities
 * while reusing the same service layer
 */
@RestController
@RequestMapping("/api/co-worker")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
@Slf4j
public class CoWorkerController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching co-worker dashboard data");
            CoWorkerDashboardResponse response = adminService.getCoWorkerDashboard();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching co-worker dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching co-worker dashboard data: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/products/top-selling")
    public ResponseEntity<?> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching top {} selling products", limit);
            List<ProductSummaryResponse> response = adminService.getTopSellingProducts(limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching top selling products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching top selling products: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/categories/distribution")
    public ResponseEntity<?> getCategoriesDistribution(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching categories distribution");
            List<CategorySummaryResponse> response = adminService.getCategoriesDistribution();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching categories distribution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching categories distribution: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/orders/stats")
    public ResponseEntity<?> getOrderStatsByStatus(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching order stats by status");
            Map<String, Long> response = adminService.getOrderStatsByStatus();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching order stats by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching order stats by status: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
}