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
public class CategoryResponse {
    private UUID categoryId;
    private String name;
    private String description;
    private UUID parentId;
    private String parentName;
    private Boolean hasSubcategories;
    private Integer subcategoryCount;
    private Integer productCount;
} 