package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.OrderItem;
import com.shopsphere.shopsphere.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);

    @Query("SELECT oi.product.productId, SUM(oi.quantity) as totalSold FROM OrderItem oi GROUP BY oi.product.productId ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts();

    @Query("SELECT oi.product.productId, SUM(oi.quantity) as totalSold FROM OrderItem oi GROUP BY oi.product.productId ORDER BY totalSold DESC LIMIT ?1")
    List<Object[]> findTopSellingProducts(int limit);
}