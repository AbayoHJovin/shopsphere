package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPaymentRequest {
    
    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "USD";
    
    @NotBlank(message = "Customer email is required")
    private String customerEmail;
    
    private String description;
    
    private String receiptEmail;
} 