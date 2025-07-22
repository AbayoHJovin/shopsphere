package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingCreateRequest {
    
    @NotNull(message = "Product ID must not be null")
    private UUID productId;
    
    @NotNull(message = "Rating stars must not be null")
    @Min(value = 1, message = "Rating stars must be at least 1")
    @Max(value = 5, message = "Rating stars must not exceed 5")
    private Integer stars;
    
    private String comment;
} 