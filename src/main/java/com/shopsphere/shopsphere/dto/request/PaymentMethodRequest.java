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
public class PaymentMethodRequest {
    
    @NotBlank(message = "Payment method type is required")
    private String type; // "credit_card" or "mtn_momo"
    
    // Credit card fields
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;
    
    // Mobile money fields
    private String mobileNumber;
    private String provider; // "MTN"
} 