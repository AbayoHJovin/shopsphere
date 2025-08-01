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
@Table(name = "delivery_regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRegion {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "region_id", updatable = false, nullable = false)
    private UUID regionId;
    
    @Column(nullable = false)
    private String regionName;
    
    @Column(nullable = false)
    private BigDecimal additionalDeliveryCost;
    
    private Boolean isActive;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private DeliveryCountry country;
    
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliverySubRegion> subRegions = new ArrayList<>();
    
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
    public void addSubRegion(DeliverySubRegion subRegion) {
        subRegions.add(subRegion);
        subRegion.setRegion(this);
    }
    
    public void removeSubRegion(DeliverySubRegion subRegion) {
        subRegions.remove(subRegion);
        subRegion.setRegion(null);
    }
} 