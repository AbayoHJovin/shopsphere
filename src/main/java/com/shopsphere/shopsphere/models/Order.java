package com.shopsphere.shopsphere.models;

import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID orderId;

    @ManyToOne
    @JoinColumn(name = "orderer_id")
    private User orderer;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 15)
    private String phoneNo;

    @Column(nullable = false)
    private Boolean approved = false;

    private String orderDate;

    private Float latitude;

    private Float longitude;

    @Column(length = 255)
    private String street;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    private String mapAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderPaymentStatus paymentStatus = OrderPaymentStatus.PENDING;

    @Column(nullable = false)
    private Boolean hasUserProven = false;

    @Column(nullable = false, unique = true)
    private String orderCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderTransaction transaction;
}