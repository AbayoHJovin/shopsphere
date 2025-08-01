package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverySubRegionRequest {
    @NotBlank(message = "Sub-region name is required")
    private String subRegionName;
    
    @NotNull(message = "Additional delivery cost is required")
    @PositiveOrZero(message = "Additional delivery cost must be zero or positive")
    private BigDecimal additionalDeliveryCost;
    
    @NotNull(message = "Region ID is required")
    private UUID regionId;
    
    private Boolean isActive;
} 