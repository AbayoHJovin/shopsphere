package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryResponse {
    private UUID categoryId;
    private String name;
    private Integer productCount;
    private Boolean hasSubcategories;
    private Long totalSold; // Added for admin dashboard usage
    private Double percentageOfTotalSales; // Added for admin dashboard usagelog
}