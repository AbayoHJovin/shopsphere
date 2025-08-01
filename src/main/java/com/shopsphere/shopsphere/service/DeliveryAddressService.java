package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.DeliveryCountryRequest;
import com.shopsphere.shopsphere.dto.request.DeliveryRegionRequest;
import com.shopsphere.shopsphere.dto.request.DeliverySubRegionRequest;
import com.shopsphere.shopsphere.dto.response.DeliveryCountryResponse;
import com.shopsphere.shopsphere.dto.response.DeliveryRegionResponse;
import com.shopsphere.shopsphere.dto.response.DeliverySubRegionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DeliveryAddressService {
    
    // Country operations
    DeliveryCountryResponse createCountry(DeliveryCountryRequest request);
    
    DeliveryCountryResponse updateCountry(UUID countryId, DeliveryCountryRequest request);
    
    void deleteCountry(UUID countryId);
    
    DeliveryCountryResponse getCountryById(UUID countryId);
    
    List<DeliveryCountryResponse> getAllActiveCountries();
    
    Page<DeliveryCountryResponse> getAllCountries(Pageable pageable);
    
    Page<DeliveryCountryResponse> searchCountries(String keyword, Pageable pageable);
    
    // Region operations
    DeliveryRegionResponse createRegion(DeliveryRegionRequest request);
    
    DeliveryRegionResponse updateRegion(UUID regionId, DeliveryRegionRequest request);
    
    void deleteRegion(UUID regionId);
    
    DeliveryRegionResponse getRegionById(UUID regionId);
    
    List<DeliveryRegionResponse> getRegionsByCountryId(UUID countryId);
    
    Page<DeliveryRegionResponse> getAllRegions(Pageable pageable);
    
    Page<DeliveryRegionResponse> searchRegions(String keyword, Pageable pageable);
    
    // SubRegion operations
    DeliverySubRegionResponse createSubRegion(DeliverySubRegionRequest request);
    
    DeliverySubRegionResponse updateSubRegion(UUID subRegionId, DeliverySubRegionRequest request);
    
    void deleteSubRegion(UUID subRegionId);
    
    DeliverySubRegionResponse getSubRegionById(UUID subRegionId);
    
    List<DeliverySubRegionResponse> getSubRegionsByRegionId(UUID regionId);
    
    Page<DeliverySubRegionResponse> getAllSubRegions(Pageable pageable);
    
    Page<DeliverySubRegionResponse> searchSubRegions(String keyword, Pageable pageable);
    
    // Utility operations
    BigDecimal calculateDeliveryCost(UUID subRegionId);
    
    BigDecimal calculateDeliveryCost(UUID countryId, UUID regionId, UUID subRegionId);
} 