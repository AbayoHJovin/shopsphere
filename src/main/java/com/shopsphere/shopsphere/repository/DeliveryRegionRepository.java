package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.DeliveryCountry;
import com.shopsphere.shopsphere.models.DeliveryRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRegionRepository extends JpaRepository<DeliveryRegion, UUID> {
    List<DeliveryRegion> findByCountry(DeliveryCountry country);
    
    List<DeliveryRegion> findByCountryCountryId(UUID countryId);
    
    List<DeliveryRegion> findByIsActiveTrue();
    
    Page<DeliveryRegion> findByIsActiveTrue(Pageable pageable);
    
    Page<DeliveryRegion> findByCountryAndIsActiveTrue(DeliveryCountry country, Pageable pageable);
    
    Page<DeliveryRegion> findByCountryCountryIdAndIsActiveTrue(UUID countryId, Pageable pageable);
    
    List<DeliveryRegion> findByCountryCountryIdAndIsActiveTrue(UUID countryId);
    
    Optional<DeliveryRegion> findByRegionNameAndCountryCountryId(String regionName, UUID countryId);
    
    Page<DeliveryRegion> findByRegionNameContainingIgnoreCase(String regionName, Pageable pageable);
} 