package com.shopsphere.shopsphere.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "config_id", updatable = false, nullable = false)
    private UUID configId;
    
    @Column(nullable = false)
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
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal minPurchaseForFreeDelivery = BigDecimal.ZERO;
    
    // Store currency code (e.g., USD, EUR)
    @Column(nullable = false)
    @Builder.Default
    private String currencyCode = "USD";
    
    // Store open hours
    private String businessHours;
    
    // Tracking fields
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String updatedBy;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 