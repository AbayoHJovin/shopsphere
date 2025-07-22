package com.shopsphere.shopsphere.models;

import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTransaction {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false, unique = true)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderPaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}