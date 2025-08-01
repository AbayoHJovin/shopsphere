package com.shopsphere.shopsphere.dto.request;

import com.shopsphere.shopsphere.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private Gender gender;

    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    @Builder.Default
    private Integer stock = 0;

    @Builder.Default
    private Boolean popular = false;

    @Builder.Default
    private List<UUID> colorIds = new ArrayList<>();
    
    @Builder.Default
    private List<ProductSizeRequest> sizes = new ArrayList<>();

    @Builder.Default
    private List<UUID> categoryIds = new ArrayList<>();
}