package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID productId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal previousPrice;
    private Gender gender;
    private Integer stock;
    private Boolean popular;
    private List<ProductImageResponse> images;
    private List<CategoryResponse> categories;
    private String mainImage; // URL of the main product image for quick access
    private Double averageRating;
    private Integer ratingCount;
    private List<RatingResponse> topRatings;
    
    // Color and size options
    private List<ProductColorResponse> colors;
    private List<ProductSizeResponse> sizes;
    
    // Discount information
    private List<ProductDiscountResponse> discounts;
    private ProductDiscountResponse activeDiscount; // Currently active discount if any
    private BigDecimal discountedPrice; // Price after applying the active discount
    private Boolean onSale; // True if there's an active discount
}