package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String name;
    private BigDecimal price;
    private BigDecimal previousPrice;
    private String imageUrl;
    private Integer quantity;
    private Integer stock;
    private BigDecimal totalPrice;
    private Double averageRating;
    private Integer ratingCount;
} 