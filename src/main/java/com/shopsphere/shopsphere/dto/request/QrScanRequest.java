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
public class QrScanRequest {

    @NotBlank(message = "Order code is required")
    private String orderCode;
} 