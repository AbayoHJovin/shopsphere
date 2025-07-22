package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    
    private UUID orderId;
    
    // Only returned for the owner of the order or admin/co-worker
    private String orderCode;
    
    private OrderStatus orderStatus;
    private OrderPaymentStatus paymentStatus;
    
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
    
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    
    private boolean isQrScanned;
    
    // Customer information
    private UserSummaryResponse user; // Null for guest orders
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    // Address information
    private String streetAddress;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;
    
    private String notes;
    
    // Items
    private List<OrderItemResponse> items;
    
    // Payment information
    private OrderTransactionResponse transaction;
} 