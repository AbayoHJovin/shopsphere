package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simplified discount information for inclusion in ProductResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscountResponse {
    private UUID discountId;
    private String name;
    private BigDecimal percentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private boolean current; // Is discount currently active based on dates
} 