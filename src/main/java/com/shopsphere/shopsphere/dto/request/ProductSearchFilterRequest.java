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
    private List<String> categories;  // Changed from UUID to String for category names
    private List<UUID> categoryIds;   // Keep this for backward compatibility
    
    // Color filtershgh
    private List<String> colors;
    
    // Size filters
    private List<String> sizes;
    
    // Discount filters
    private List<String> discountRanges; // String format like "1% - 20%", "21% - 40%", etc.
    
    // Rating filters
    private Double rating;            // Single rating value (e.g., 4 means 4+ stars)
    private Double minRating;         // For backward compatibility
    private Double maxRating;         // For backward compatibility
    
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
    
    // Helper method to parse discount ranges for backend processing
    public List<DiscountRange> parseDiscountRanges() {
        if (discountRanges == null || discountRanges.isEmpty()) {
            return List.of();
        }
        
        return discountRanges.stream()
            .map(range -> {
                // Parse discount ranges like "1% - 20%", "21% - 40%", "Over 60%", etc.
                if (range.contains("-")) {
                    String[] parts = range.replace("%", "").split("-");
                    int min = Integer.parseInt(parts[0].trim());
                    int max = Integer.parseInt(parts[1].trim());
                    return new DiscountRange(min, max);
                } else if (range.toLowerCase().contains("over")) {
                    // Handle "Over X%" format
                    String numStr = range.toLowerCase().replace("over", "")
                            .replace("%", "").trim();
                    int min = Integer.parseInt(numStr);
                    return new DiscountRange(min, 100);
                }
                return null;
            })
            .filter(range -> range != null)
            .toList();
    }
    
    // Inner class to represent a discount range
    @Data
    @AllArgsConstructor
    public static class DiscountRange {
        private int min;
        private int max;
    }
} 