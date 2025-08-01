package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationRequest {
    
    @NotBlank(message = "Business name is required")
    private String businessName;
    
    // Contact information
    private String businessPhone;
    
    @Email(message = "Please provide a valid email address")
    private String businessEmail;
    
    // Social media links
    private String facebookUrl;
    private String instagramUrl;
    private String twitterUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    
    // Business location
    private String streetAddress;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;
    
    // Free delivery settings
    @PositiveOrZero(message = "Minimum purchase amount for free delivery must be zero or positive")
    private BigDecimal minPurchaseForFreeDelivery;
    
    // Store currency code (e.g., USD, EUR)
    private String currencyCode;
    
    // Store open hours
    private String businessHours;
} 