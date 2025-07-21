package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.OrderStatus;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByOrderer(User orderer);

    Page<Order> findByOrderer(User orderer, Pageable pageable);

    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);
}