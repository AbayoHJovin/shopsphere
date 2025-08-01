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
public class ConfigurationResponse {
    
    private UUID configId;
    private String businessName;
    
    // Contact information
    private String businessPhone;
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
    private BigDecimal minPurchaseForFreeDelivery;
    
    // Store currency code (e.g., USD, EUR)
    private String currencyCode;
    
    // Store open hours
    private String businessHours;
    
    // Tracking fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
} 