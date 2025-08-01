package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.QrScanRequest;
import com.shopsphere.shopsphere.dto.response.AdminDashboardResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.dto.response.CoWorkerDashboardResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import com.shopsphere.shopsphere.dto.response.ProductSummaryResponse;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.AdminService;
import com.shopsphere.shopsphere.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Unified controller for ADMIN and CO_WORKER operations
 * Serves different content based on user role
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final AdminService adminService;
    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> getDashboard(Authentication authentication, HttpServletRequest servletRequest) {
        try {
            // Check if user is an admin
            boolean isAdmin = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            if (isAdmin) {
                log.info("Fetching admin dashboard data");
                AdminDashboardResponse response = adminService.getDashboardAnalytics();
                return ResponseEntity.ok(response);
            } else {
                log.info("Fetching co-worker dashboard data");
                CoWorkerDashboardResponse response = adminService.getCoWorkerDashboard();
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error fetching dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching dashboard data: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/products/top-selling")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
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
    
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching revenue data from {} to {}", startDate, endDate);
            Map<String, Object> response = adminService.getRevenueByDateRange(startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching revenue data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching revenue data: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/users/growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserGrowthByMonth(
            @RequestParam(defaultValue = "6") int months,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching user growth data for the last {} months", months);
            Map<String, Long> response = adminService.getUserGrowthByMonth(months);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching user growth data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching user growth data: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
    
    @PostMapping("/orders/scan-qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> scanQrCode(
            @Valid @RequestBody QrScanRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Scanning QR code to verify and deliver order");
            String userEmail = authentication.getName();
            
            OrderResponse response = orderService.verifyQrCodeAndDeliver(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Order not found or QR code invalid", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalStateException e) {
            log.error("Invalid state", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error scanning QR code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error scanning QR code: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
} 