package com.shopsphere.shopsphere.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery_countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCountry {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "country_id", updatable = false, nullable = false)
    private UUID countryId;
    
    @Column(nullable = false, unique = true)
    private String countryName;
    
    @Column(nullable = false)
    private String countryCode;
    
    @Column(nullable = false)
    private BigDecimal baseDeliveryCost;
    
    private Boolean isActive;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryRegion> regions = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods to manage bidirectional relationship
    public void addRegion(DeliveryRegion region) {
        regions.add(region);
        region.setCountry(this);
    }
    
    public void removeRegion(DeliveryRegion region) {
        regions.remove(region);
        region.setCountry(null);
    }
} 