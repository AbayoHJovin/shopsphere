package com.shopsphere.shopsphere.dto.request;

import com.shopsphere.shopsphere.enums.Gender;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {
    private String name;

    private String description;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private Gender gender;

    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    private Integer stock;

    private Boolean popular;

    @Builder.Default
    private List<UUID> colorIds = null;
    
    @Builder.Default
    private List<ProductSizeRequest> sizes = null;
    
    private List<UUID> categoryIds;
}