package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.OrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderTransactionRepository extends JpaRepository<OrderTransaction, UUID> {
    Optional<OrderTransaction> findByOrder(Order order);
}