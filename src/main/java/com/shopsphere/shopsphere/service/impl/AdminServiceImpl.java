package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.response.AdminDashboardResponse;
import com.shopsphere.shopsphere.dto.response.CategorySummaryResponse;
import com.shopsphere.shopsphere.dto.response.CoWorkerDashboardResponse;
import com.shopsphere.shopsphere.dto.response.ProductSummaryResponse;
import com.shopsphere.shopsphere.enums.OrderStatus;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.models.*;
import com.shopsphere.shopsphere.repository.*;
import com.shopsphere.shopsphere.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTransactionRepository orderTransactionRepository;
    private final CartRepository cartRepository;

    @Override
    public AdminDashboardResponse getDashboardAnalytics() {
        log.info("Generating admin dashboard analytics");

        // Get the current date and first day of current month
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0);
        LocalDateTime firstDayOfLastMonth = firstDayOfMonth.minusMonths(1);
        LocalDateTime firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);

        // User statistics
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long totalCoWorkers = userRepository.countByRole(Role.CO_WORKER);
        long newUsersThisMonth = userRepository.countByCreatedAtBetween(firstDayOfMonth, now);

        // Product statistics
        long totalProducts = productRepository.count();
        long outOfStockProducts = productRepository.countByStockEquals(0);
        long lowStockProducts = productRepository.countByStockLessThan(10); // Assuming less than 10 is low stock
        List<ProductSummaryResponse> topSellingProducts = getTopSellingProducts(5);
        List<CategorySummaryResponse> categoriesDistribution = getCategoriesDistribution();

        // Order statistics
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long deliveringOrders = orderRepository.countByOrderStatus(OrderStatus.PROCESSING);
        long deliveredOrders = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);
        long ordersThisMonth = orderRepository.countByOrderDateBetween(firstDayOfMonth, now);

        // Revenue statistics
        BigDecimal totalRevenue = calculateTotalRevenue();
        BigDecimal revenueThisMonth = calculateRevenueForDateRange(firstDayOfMonth, firstDayOfNextMonth);
        BigDecimal revenueLastMonth = calculateRevenueForDateRange(firstDayOfLastMonth, firstDayOfMonth);
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        List<Map<String, Object>> revenueByMonth = calculateRevenueByMonth(6); // Last 6 months

        // Cart statistics
        long activeCartsCount = cartRepository.count();
        BigDecimal totalCartValue = calculateTotalCartValue();
        double cartAbandonmentRate = calculateCartAbandonmentRate();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalAdmins(totalAdmins)
                .totalCoWorkers(totalCoWorkers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalProducts(totalProducts)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts(lowStockProducts)
                .topSellingProducts(topSellingProducts)
                .categoriesDistribution(categoriesDistribution)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveringOrders(deliveringOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .ordersThisMonth(ordersThisMonth)
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .averageOrderValue(averageOrderValue)
                .revenueByMonth(revenueByMonth)
                .activeCartsCount(activeCartsCount)
                .totalCartValue(totalCartValue)
                .cartAbandonmentRate(cartAbandonmentRate)
                .build();
    }

    @Override
    public List<ProductSummaryResponse> getTopSellingProducts(int limit) {
        log.info("Fetching top {} selling products", limit);

        // Get all order items
        List<OrderItem> allOrderItems = orderItemRepository.findAll();

        // Group by product and calculate totals
        Map<Product, Integer> productSalesCount = new HashMap<>();
        Map<Product, BigDecimal> productRevenue = new HashMap<>();

        for (OrderItem item : allOrderItems) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            BigDecimal itemRevenue = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            productSalesCount.merge(product, quantity, Integer::sum);
            productRevenue.merge(product, itemRevenue, BigDecimal::add);
        }

        // Convert to list and sort by sales count
        return productSalesCount.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Product product = entry.getKey();
                    int totalSold = entry.getValue();
                    BigDecimal totalRev = productRevenue.getOrDefault(product, BigDecimal.ZERO);

                    // Get main image URL if available
                    String imageUrl = product.getImages().stream()
                            .filter(ProductImage::getIsMain)
                            .findFirst()
                            .map(ProductImage::getImageUrl)
                            .orElse(null);

                    return ProductSummaryResponse.builder()
                            .productId(product.getProductId())
                            .name(product.getName())
                            .imageUrl(imageUrl)
                            .price(product.getPrice())
                            .totalSold(totalSold)
                            .totalRevenue(totalRev)
                            .stock(product.getStock())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySummaryResponse> getCategoriesDistribution() {
        log.info("Fetching categories distribution");

        // Get all categories
        List<Category> allCategories = categoryRepository.findAll();

        // Get total sales across all categories
        long totalSales = orderItemRepository.count();

        // Calculate sales per category
        return allCategories.stream()
                .map(category -> {
                    // Get all products in this category
                    List<Product> products = category.getProducts();
                    int productCount = products.size(); // Changed from long to int

                    // Calculate total sales for this category
                    long categorySales = products.stream()
                            .flatMap(product -> product.getOrderItems().stream())
                            .mapToLong(OrderItem::getQuantity)
                            .sum();

                    // Calculate percentage of total sales
                    double percentage = totalSales > 0
                            ? (double) categorySales / totalSales * 100
                            : 0.0;

                    return CategorySummaryResponse.builder()
                            .categoryId(category.getCategoryId())
                            .name(category.getName())
                            .productCount(productCount)
                            .totalSold(categorySales)
                            .percentageOfTotalSales(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(CategorySummaryResponse::getTotalSold).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating revenue from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        BigDecimal revenue = calculateRevenueForDateRange(startDateTime, endDateTime);
        long orderCount = orderRepository.countByOrderDateBetween(startDateTime, endDateTime);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("revenue", revenue);
        result.put("orderCount", orderCount);

        return result;
    }

    @Override
    public Map<String, Long> getUserGrowthByMonth(int months) {
        log.info("Calculating user growth for the last {} months", months);

        Map<String, Long> userGrowth = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);

            if (monthEnd.isAfter(now)) {
                monthEnd = now;
            }

            long newUsers = userRepository.countByCreatedAtBetween(monthStart, monthEnd);
            String monthKey = monthStart.getMonth() + " " + monthStart.getYear();
            userGrowth.put(monthKey, newUsers);
        }

        return userGrowth;
    }

    @Override
    public Map<String, Long> getOrderStatsByStatus() {
        log.info("Fetching order statistics by status");

        Map<String, Long> orderStats = new LinkedHashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByOrderStatus(status);
            orderStats.put(status.name(), count);
        }

        return orderStats;
    }

    @Override
    public CoWorkerDashboardResponse getCoWorkerDashboard() {
        log.info("Generating co-worker dashboard data");

        // Count customers
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);

        // Count co-workers
        long totalCoWorkers = userRepository.countByRole(Role.CO_WORKER);

        // Count products and stock status
        long totalProducts = productRepository.count();
        long outOfStockProducts = productRepository.countByStockEquals(0);
        long lowStockProducts = productRepository.countByStockLessThan(10) - outOfStockProducts;

        // Count orders by status
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long shippedOrders = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);

        // Get order stats by status
        Map<String, Long> orderStatsByStatus = getOrderStatsByStatus();

        // Get top selling products (limited to 5)
        List<ProductSummaryResponse> topSellingProducts = getTopSellingProducts(5);

        // Get category distribution
        List<CategorySummaryResponse> categoryDistribution = getCategoriesDistribution();

        return CoWorkerDashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .totalCoWorkers(totalCoWorkers)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .orderStatsByStatus(orderStatsByStatus)
                .topSellingProducts(topSellingProducts)
                .categoryDistribution(categoryDistribution)
                .build();
    }

    // Helper methods
    private BigDecimal calculateTotalRevenue() {
        List<OrderTransaction> transactions = orderTransactionRepository.findAll();
        return transactions.stream()
                .map(OrderTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<OrderTransaction> transactions = orderTransactionRepository.findByTransactionDateBetween(startDate,
                endDate);
        return transactions.stream()
                .map(OrderTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> calculateRevenueByMonth(int months) {
        List<Map<String, Object>> revenueByMonth = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay();

            if (monthEnd.isAfter(now)) {
                monthEnd = now;
            }

            BigDecimal revenue = calculateRevenueForDateRange(monthStart, monthEnd);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthStart.format(formatter));
            monthData.put("revenue", revenue);

            revenueByMonth.add(monthData);
        }

        return revenueByMonth;
    }

    private BigDecimal calculateTotalCartValue() {
        List<Cart> activeCarts = cartRepository.findAll();

        return activeCarts.stream()
                .flatMap(cart -> cart.getCartProducts().stream())
                .map(cartProduct -> {
                    BigDecimal productPrice = cartProduct.getProduct().getPrice();
                    int quantity = cartProduct.getQuantity();
                    return productPrice.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateCartAbandonmentRate() {
        long totalCarts = cartRepository.count();
        if (totalCarts == 0) {
            return 0.0;
        }

        long convertedCarts = orderRepository.count(); // Simplified: assuming each order came from a cart

        return (double) (totalCarts - convertedCarts) / totalCarts * 100;
    }
}