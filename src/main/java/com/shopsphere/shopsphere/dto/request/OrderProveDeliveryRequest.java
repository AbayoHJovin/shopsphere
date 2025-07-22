package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderProveDeliveryRequest {

    @NotBlank(message = "Order code is required for guest users")
    private String orderCode;

    private String feedback;
}