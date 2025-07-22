package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.PaymentStatus;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    List<Payment> findByOrder(Order order);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByOrderAndStatus(Order order, PaymentStatus status);
} 