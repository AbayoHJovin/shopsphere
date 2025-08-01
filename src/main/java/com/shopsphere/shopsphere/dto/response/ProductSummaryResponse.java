package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummaryResponse {
    private UUID productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal previousPrice;
    private int totalSold;
    private BigDecimal totalRevenue;
    private int stock;
    private Double averageRating;
    private Integer ratingCount;
    private Integer colorCount; // Number of available colors
    private Integer sizeCount;  // Number of available sizes
}