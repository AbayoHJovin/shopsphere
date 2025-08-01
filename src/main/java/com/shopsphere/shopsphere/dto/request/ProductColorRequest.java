package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductColorRequest {
    
    @NotBlank(message = "Color name is required")
    private String colorName;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color hex code must be a valid hex color (e.g., #FF5733)")
    private String colorHexCode;
    
    // Removed productId field as colors are now independent of products
}