package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverySubRegionResponse {
    private UUID subRegionId;
    private String subRegionName;
    private BigDecimal additionalDeliveryCost;
    private UUID regionId;
    private String regionName;
    private UUID countryId;
    private String countryName;
    private BigDecimal totalDeliveryCost; // Sum of all delivery costs in hierarchy
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 