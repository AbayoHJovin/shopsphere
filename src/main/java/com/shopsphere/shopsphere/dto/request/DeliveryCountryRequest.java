package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryCountryRequest {
    @NotBlank(message = "Country name is required")
    private String countryName;
    
    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country code must be 2 or 3 uppercase letters")
    private String countryCode;
    
    @NotNull(message = "Base delivery cost is required")
    @Positive(message = "Base delivery cost must be positive")
    private BigDecimal baseDeliveryCost;
    
    private Boolean isActive;
} 