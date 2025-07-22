package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountUpdateRequest {

    private String name;

    private String description;

    @Min(value = 1, message = "Discount percentage must be at least 1%")
    private BigDecimal percentage;

    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    private List<UUID> productIds;

    private Boolean active;
}