package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.OrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderTransactionRepository extends JpaRepository<OrderTransaction, UUID> {

    Optional<OrderTransaction> findByOrder(Order order);

    List<OrderTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<OrderTransaction> findByStatus(OrderPaymentStatus status);

    Optional<OrderTransaction> findByTransactionReference(String transactionReference);
}