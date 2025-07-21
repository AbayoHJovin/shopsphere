package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.OrderItem;
import com.shopsphere.shopsphere.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder(Order order);

    Optional<OrderItem> findByOrderAndProduct(Order order, Product product);
}