package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.OrderStatus;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    List<Order> findByUserIsNull();
    
    boolean existsByOrderCode(String orderCode);
    
    long countByOrderStatus(OrderStatus orderStatus);
    
    long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByIsQrScanned(boolean isQrScanned);
    
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
}