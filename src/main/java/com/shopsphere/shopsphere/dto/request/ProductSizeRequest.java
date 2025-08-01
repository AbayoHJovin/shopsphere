package com.shopsphere.shopsphere.dto.request;

import com.shopsphere.shopsphere.enums.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeRequest {
    
    @NotNull(message = "Size is required")
    private Size size;
    
    @NotNull(message = "Stock for size is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockForSize;
} 