package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
    // User statistics
    private long totalUsers;
    private long totalCustomers;
    private long totalAdmins;
    private long totalCoWorkers;
    private long newUsersThisMonth;

    // Product statistics
    private long totalProducts;
    private long outOfStockProducts;
    private long lowStockProducts;
    private List<ProductSummaryResponse> topSellingProducts;
    private List<CategorySummaryResponse> categoriesDistribution;

    // Order statistics
    private long totalOrders;
    private long pendingOrders;
    private long deliveringOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long ordersThisMonth;

    // Revenue statistics
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueLastMonth;
    private BigDecimal averageOrderValue;
    private List<Map<String, Object>> revenueByMonth;

    // Cart statistics
    private long activeCartsCount;
    private BigDecimal totalCartValue;
    private double cartAbandonmentRate;
}