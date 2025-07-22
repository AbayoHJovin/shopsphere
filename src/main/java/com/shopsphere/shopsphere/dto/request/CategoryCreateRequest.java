package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    
    private UUID parentId; // For creating subcategories, null for top-level categories
} 