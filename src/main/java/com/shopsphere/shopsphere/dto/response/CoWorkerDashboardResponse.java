package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoWorkerDashboardResponse {
    private long totalCustomers;
    private long totalCoWorkers;
    private long totalProducts;
    private long lowStockProducts;
    private long outOfStockProducts;
    private long totalOrders;
    private long pendingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private Map<String, Long> orderStatsByStatus;
    private List<ProductSummaryResponse> topSellingProducts;
    private List<CategorySummaryResponse> categoryDistribution;
}