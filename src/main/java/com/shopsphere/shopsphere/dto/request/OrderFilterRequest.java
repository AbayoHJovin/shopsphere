package com.shopsphere.shopsphere.dto.request;

import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderFilterRequest {

    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    // Status filters
    private OrderStatus orderStatus;
    private OrderPaymentStatus paymentStatus;

    // Address filters
    private String city;
    private String stateProvince;
    private String country;

    // Delivery proof filter
    private Boolean hasUserProven;

    // Customer filter
    private String customerEmail;

    // Order code filter (for admins only)
    private String orderCode;
}