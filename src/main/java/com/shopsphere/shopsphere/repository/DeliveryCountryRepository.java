package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.DeliveryCountry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryCountryRepository extends JpaRepository<DeliveryCountry, UUID> {
    Optional<DeliveryCountry> findByCountryName(String countryName);
    
    Optional<DeliveryCountry> findByCountryCode(String countryCode);
    
    List<DeliveryCountry> findByIsActiveTrue();
    
    Page<DeliveryCountry> findByIsActiveTrue(Pageable pageable);
    
    Page<DeliveryCountry> findByCountryNameContainingIgnoreCase(String countryName, Pageable pageable);
} 