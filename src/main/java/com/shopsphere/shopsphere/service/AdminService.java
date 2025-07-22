package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.response.AdminDashboardResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.dto.response.CoWorkerDashboardResponse;
import com.shopsphere.shopsphere.dto.response.ProductSummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdminService {
    AdminDashboardResponse getDashboardAnalytics();

    List<ProductSummaryResponse> getTopSellingProducts(int limit);

    List<CategorySummaryResponse> getCategoriesDistribution();

    Map<String, Long> getOrderStatsByStatus();

    Map<String, Object> getRevenueByDateRange(LocalDate startDate, LocalDate endDate);

    Map<String, Long> getUserGrowthByMonth(int months);

    CoWorkerDashboardResponse getCoWorkerDashboard();
}