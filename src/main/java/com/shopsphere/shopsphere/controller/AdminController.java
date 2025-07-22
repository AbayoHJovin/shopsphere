package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.AdminDashboardResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.dto.response.ProductSummaryResponse;
import com.shopsphere.shopsphere.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboardAnalytics() {
        return ResponseEntity.ok(adminService.getDashboardAnalytics());
    }

    @GetMapping("/products/top-selling")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<List<ProductSummaryResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(adminService.getTopSellingProducts(limit));
    }

    @GetMapping("/categories/distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<List<CategorySummaryResponse>> getCategoriesDistribution() {
        return ResponseEntity.ok(adminService.getCategoriesDistribution());
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminService.getRevenueByDateRange(startDate, endDate));
    }

    @GetMapping("/users/growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getUserGrowthByMonth(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(adminService.getUserGrowthByMonth(months));
    }

    @GetMapping("/orders/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<Map<String, Long>> getOrderStatsByStatus() {
        return ResponseEntity.ok(adminService.getOrderStatsByStatus());
    }
}