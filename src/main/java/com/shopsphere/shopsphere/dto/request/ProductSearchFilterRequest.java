package com.shopsphere.shopsphere.dto.request;

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
public class ProductSearchFilterRequest {
    // Text search
    private String keyword;
    
    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Category filters
    private List<UUID> categoryIds;
    
    // Rating filters
    private Double minRating;
    private Double maxRating;
    
    // Stock filters
    private Boolean inStock;
    
    // Special filters
    private Boolean onSale; // Has discount/previous price
    private Boolean newArrivals; // Recently added products
    private Boolean popular; // Products marked as popular
    
    // Gender filter
    private Gender gender;
    
    // Sorting
    private String sortBy;
    private String sortDirection;
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 10;
} 