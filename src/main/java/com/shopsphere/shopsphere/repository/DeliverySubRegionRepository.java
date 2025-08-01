package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.DeliveryRegion;
import com.shopsphere.shopsphere.models.DeliverySubRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliverySubRegionRepository extends JpaRepository<DeliverySubRegion, UUID> {
    List<DeliverySubRegion> findByRegion(DeliveryRegion region);
    
    List<DeliverySubRegion> findByRegionRegionId(UUID regionId);
    
    List<DeliverySubRegion> findByIsActiveTrue();
    
    Page<DeliverySubRegion> findByIsActiveTrue(Pageable pageable);
    
    Page<DeliverySubRegion> findByRegionAndIsActiveTrue(DeliveryRegion region, Pageable pageable);
    
    Page<DeliverySubRegion> findByRegionRegionIdAndIsActiveTrue(UUID regionId, Pageable pageable);
    
    List<DeliverySubRegion> findByRegionRegionIdAndIsActiveTrue(UUID regionId);
    
    Optional<DeliverySubRegion> findBySubRegionNameAndRegionRegionId(String subRegionName, UUID regionId);
    
    Page<DeliverySubRegion> findBySubRegionNameContainingIgnoreCase(String subRegionName, Pageable pageable);
    
    @Query("SELECT sr FROM DeliverySubRegion sr JOIN sr.region r JOIN r.country c WHERE c.countryId = :countryId AND sr.isActive = true")
    Page<DeliverySubRegion> findByCountryId(@Param("countryId") UUID countryId, Pageable pageable);
} 