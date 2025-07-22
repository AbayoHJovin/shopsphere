package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class MomoPaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+[0-9]{1,15}$", message = "Mobile number must be in international format with + prefix")
    private String mobileNumber;
    
    @Builder.Default
    private String currency = "EUR"; // Using EUR as the default currency for MTN MoMo
    
    @NotBlank(message = "Customer email is required")
    private String customerEmail;
    
    private String description;
} 