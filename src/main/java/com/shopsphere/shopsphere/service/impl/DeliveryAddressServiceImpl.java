package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.DeliveryCountryRequest;
import com.shopsphere.shopsphere.dto.request.DeliveryRegionRequest;
import com.shopsphere.shopsphere.dto.request.DeliverySubRegionRequest;
import com.shopsphere.shopsphere.dto.response.DeliveryCountryResponse;
import com.shopsphere.shopsphere.dto.response.DeliveryRegionResponse;
import com.shopsphere.shopsphere.dto.response.DeliverySubRegionResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.DeliveryCountry;
import com.shopsphere.shopsphere.models.DeliveryRegion;
import com.shopsphere.shopsphere.models.DeliverySubRegion;
import com.shopsphere.shopsphere.repository.DeliveryCountryRepository;
import com.shopsphere.shopsphere.repository.DeliveryRegionRepository;
import com.shopsphere.shopsphere.repository.DeliverySubRegionRepository;
import com.shopsphere.shopsphere.service.DeliveryAddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAddressServiceImpl implements DeliveryAddressService {

    private final DeliveryCountryRepository countryRepository;
    private final DeliveryRegionRepository regionRepository;
    private final DeliverySubRegionRepository subRegionRepository;

    //-------------------- Country Operations --------------------//
    @Override
    @Transactional
    public DeliveryCountryResponse createCountry(DeliveryCountryRequest request) {
        log.info("Creating new delivery country: {}", request.getCountryName());
        
        // Check if country already exists
        Optional<DeliveryCountry> existingCountry = countryRepository.findByCountryName(request.getCountryName());
        if (existingCountry.isPresent()) {
            throw new IllegalArgumentException("A country with this name already exists: " + request.getCountryName());
        }
        
        // Check country code
        existingCountry = countryRepository.findByCountryCode(request.getCountryCode());
        if (existingCountry.isPresent()) {
            throw new IllegalArgumentException("A country with this code already exists: " + request.getCountryCode());
        }
        
        // Create and save country
        DeliveryCountry country = DeliveryCountry.builder()
                .countryName(request.getCountryName())
                .countryCode(request.getCountryCode())
                .baseDeliveryCost(request.getBaseDeliveryCost())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        DeliveryCountry savedCountry = countryRepository.save(country);
        
        return mapCountryToResponse(savedCountry);
    }

    @Override
    @Transactional
    public DeliveryCountryResponse updateCountry(UUID countryId, DeliveryCountryRequest request) {
        log.info("Updating delivery country with ID: {}", countryId);
        
        DeliveryCountry country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + countryId));
        
        // Check if name is being changed and if it already exists
        if (!country.getCountryName().equals(request.getCountryName())) {
            Optional<DeliveryCountry> existingCountry = countryRepository.findByCountryName(request.getCountryName());
            if (existingCountry.isPresent() && !existingCountry.get().getCountryId().equals(countryId)) {
                throw new IllegalArgumentException("A country with this name already exists: " + request.getCountryName());
            }
        }
        
        // Check if code is being changed and if it already exists
        if (!country.getCountryCode().equals(request.getCountryCode())) {
            Optional<DeliveryCountry> existingCountry = countryRepository.findByCountryCode(request.getCountryCode());
            if (existingCountry.isPresent() && !existingCountry.get().getCountryId().equals(countryId)) {
                throw new IllegalArgumentException("A country with this code already exists: " + request.getCountryCode());
            }
        }
        
        // Update country
        country.setCountryName(request.getCountryName());
        country.setCountryCode(request.getCountryCode());
        country.setBaseDeliveryCost(request.getBaseDeliveryCost());
        
        if (request.getIsActive() != null) {
            country.setIsActive(request.getIsActive());
        }
        
        DeliveryCountry updatedCountry = countryRepository.save(country);
        
        return mapCountryToResponse(updatedCountry);
    }

    @Override
    @Transactional
    public void deleteCountry(UUID countryId) {
        log.info("Deleting delivery country with ID: {}", countryId);
        
        DeliveryCountry country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + countryId));
        
        // Check if country has regions before deleting
        if (!country.getRegions().isEmpty()) {
            throw new IllegalStateException("Cannot delete country that has regions. Delete regions first or set country as inactive.");
        }
        
        countryRepository.delete(country);
    }

    @Override
    public DeliveryCountryResponse getCountryById(UUID countryId) {
        log.info("Fetching delivery country with ID: {}", countryId);
        
        DeliveryCountry country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + countryId));
        
        return mapCountryToResponse(country);
    }

    @Override
    public List<DeliveryCountryResponse> getAllActiveCountries() {
        log.info("Fetching all active delivery countries");
        
        List<DeliveryCountry> countries = countryRepository.findByIsActiveTrue();
        
        return countries.stream()
                .map(this::mapCountryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DeliveryCountryResponse> getAllCountries(Pageable pageable) {
        log.info("Fetching all delivery countries with pagination");
        
        Page<DeliveryCountry> countries = countryRepository.findAll(pageable);
        
        return countries.map(this::mapCountryToResponse);
    }

    @Override
    public Page<DeliveryCountryResponse> searchCountries(String keyword, Pageable pageable) {
        log.info("Searching delivery countries with keyword: {}", keyword);
        
        Page<DeliveryCountry> countries = countryRepository.findByCountryNameContainingIgnoreCase(keyword, pageable);
        
        return countries.map(this::mapCountryToResponse);
    }
    
    //-------------------- Region Operations --------------------//
    @Override
    @Transactional
    public DeliveryRegionResponse createRegion(DeliveryRegionRequest request) {
        log.info("Creating new delivery region: {} in country ID: {}", request.getRegionName(), request.getCountryId());
        
        // Find country
        DeliveryCountry country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + request.getCountryId()));
        
        // Check if region already exists in this country
        Optional<DeliveryRegion> existingRegion = regionRepository.findByRegionNameAndCountryCountryId(
                request.getRegionName(), request.getCountryId());
        if (existingRegion.isPresent()) {
            throw new IllegalArgumentException("A region with this name already exists in the selected country: " + request.getRegionName());
        }
        
        // Create and save region
        DeliveryRegion region = DeliveryRegion.builder()
                .regionName(request.getRegionName())
                .additionalDeliveryCost(request.getAdditionalDeliveryCost())
                .country(country)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        DeliveryRegion savedRegion = regionRepository.save(region);
        
        return mapRegionToResponse(savedRegion);
    }

    @Override
    @Transactional
    public DeliveryRegionResponse updateRegion(UUID regionId, DeliveryRegionRequest request) {
        log.info("Updating delivery region with ID: {}", regionId);
        
        DeliveryRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with ID: " + regionId));
        
        // Find country
        DeliveryCountry country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + request.getCountryId()));
        
        // Check if name is being changed and if it already exists in the same country
        if (!region.getRegionName().equals(request.getRegionName()) || 
                !region.getCountry().getCountryId().equals(request.getCountryId())) {
            Optional<DeliveryRegion> existingRegion = regionRepository.findByRegionNameAndCountryCountryId(
                    request.getRegionName(), request.getCountryId());
            if (existingRegion.isPresent() && !existingRegion.get().getRegionId().equals(regionId)) {
                throw new IllegalArgumentException("A region with this name already exists in the selected country: " + request.getRegionName());
            }
        }
        
        // Update region
        region.setRegionName(request.getRegionName());
        region.setAdditionalDeliveryCost(request.getAdditionalDeliveryCost());
        region.setCountry(country);
        
        if (request.getIsActive() != null) {
            region.setIsActive(request.getIsActive());
        }
        
        DeliveryRegion updatedRegion = regionRepository.save(region);
        
        return mapRegionToResponse(updatedRegion);
    }

    @Override
    @Transactional
    public void deleteRegion(UUID regionId) {
        log.info("Deleting delivery region with ID: {}", regionId);
        
        DeliveryRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with ID: " + regionId));
        
        // Check if region has sub-regions before deleting
        if (!region.getSubRegions().isEmpty()) {
            throw new IllegalStateException("Cannot delete region that has sub-regions. Delete sub-regions first or set region as inactive.");
        }
        
        regionRepository.delete(region);
    }

    @Override
    public DeliveryRegionResponse getRegionById(UUID regionId) {
        log.info("Fetching delivery region with ID: {}", regionId);
        
        DeliveryRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with ID: " + regionId));
        
        return mapRegionToResponse(region);
    }

    @Override
    public List<DeliveryRegionResponse> getRegionsByCountryId(UUID countryId) {
        log.info("Fetching delivery regions for country ID: {}", countryId);
        
        // Check if country exists
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("Country not found with ID: " + countryId);
        }
        
        List<DeliveryRegion> regions = regionRepository.findByCountryCountryIdAndIsActiveTrue(countryId);
        
        return regions.stream()
                .map(this::mapRegionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DeliveryRegionResponse> getAllRegions(Pageable pageable) {
        log.info("Fetching all delivery regions with pagination");
        
        Page<DeliveryRegion> regions = regionRepository.findAll(pageable);
        
        return regions.map(this::mapRegionToResponse);
    }

    @Override
    public Page<DeliveryRegionResponse> searchRegions(String keyword, Pageable pageable) {
        log.info("Searching delivery regions with keyword: {}", keyword);
        
        Page<DeliveryRegion> regions = regionRepository.findByRegionNameContainingIgnoreCase(keyword, pageable);
        
        return regions.map(this::mapRegionToResponse);
    }
    
    //-------------------- SubRegion Operations --------------------//
    @Override
    @Transactional
    public DeliverySubRegionResponse createSubRegion(DeliverySubRegionRequest request) {
        log.info("Creating new delivery sub-region: {} in region ID: {}", request.getSubRegionName(), request.getRegionId());
        
        // Find region
        DeliveryRegion region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with ID: " + request.getRegionId()));
        
        // Check if sub-region already exists in this region
        Optional<DeliverySubRegion> existingSubRegion = subRegionRepository.findBySubRegionNameAndRegionRegionId(
                request.getSubRegionName(), request.getRegionId());
        if (existingSubRegion.isPresent()) {
            throw new IllegalArgumentException("A sub-region with this name already exists in the selected region: " + request.getSubRegionName());
        }
        
        // Create and save sub-region
        DeliverySubRegion subRegion = DeliverySubRegion.builder()
                .subRegionName(request.getSubRegionName())
                .additionalDeliveryCost(request.getAdditionalDeliveryCost())
                .region(region)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        DeliverySubRegion savedSubRegion = subRegionRepository.save(subRegion);
        
        return mapSubRegionToResponse(savedSubRegion);
    }

    @Override
    @Transactional
    public DeliverySubRegionResponse updateSubRegion(UUID subRegionId, DeliverySubRegionRequest request) {
        log.info("Updating delivery sub-region with ID: {}", subRegionId);
        
        DeliverySubRegion subRegion = subRegionRepository.findById(subRegionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-region not found with ID: " + subRegionId));
        
        // Find region
        DeliveryRegion region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with ID: " + request.getRegionId()));
        
        // Check if name is being changed and if it already exists in the same region
        if (!subRegion.getSubRegionName().equals(request.getSubRegionName()) || 
                !subRegion.getRegion().getRegionId().equals(request.getRegionId())) {
            Optional<DeliverySubRegion> existingSubRegion = subRegionRepository.findBySubRegionNameAndRegionRegionId(
                    request.getSubRegionName(), request.getRegionId());
            if (existingSubRegion.isPresent() && !existingSubRegion.get().getSubRegionId().equals(subRegionId)) {
                throw new IllegalArgumentException("A sub-region with this name already exists in the selected region: " + request.getSubRegionName());
            }
        }
        
        // Update sub-region
        subRegion.setSubRegionName(request.getSubRegionName());
        subRegion.setAdditionalDeliveryCost(request.getAdditionalDeliveryCost());
        subRegion.setRegion(region);
        
        if (request.getIsActive() != null) {
            subRegion.setIsActive(request.getIsActive());
        }
        
        DeliverySubRegion updatedSubRegion = subRegionRepository.save(subRegion);
        
        return mapSubRegionToResponse(updatedSubRegion);
    }

    @Override
    @Transactional
    public void deleteSubRegion(UUID subRegionId) {
        log.info("Deleting delivery sub-region with ID: {}", subRegionId);
        
        DeliverySubRegion subRegion = subRegionRepository.findById(subRegionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-region not found with ID: " + subRegionId));
        
        subRegionRepository.delete(subRegion);
    }

    @Override
    public DeliverySubRegionResponse getSubRegionById(UUID subRegionId) {
        log.info("Fetching delivery sub-region with ID: {}", subRegionId);
        
        DeliverySubRegion subRegion = subRegionRepository.findById(subRegionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-region not found with ID: " + subRegionId));
        
        return mapSubRegionToResponse(subRegion);
    }

    @Override
    public List<DeliverySubRegionResponse> getSubRegionsByRegionId(UUID regionId) {
        log.info("Fetching delivery sub-regions for region ID: {}", regionId);
        
        // Check if region exists
        if (!regionRepository.existsById(regionId)) {
            throw new ResourceNotFoundException("Region not found with ID: " + regionId);
        }
        
        List<DeliverySubRegion> subRegions = subRegionRepository.findByRegionRegionIdAndIsActiveTrue(regionId);
        
        return subRegions.stream()
                .map(this::mapSubRegionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DeliverySubRegionResponse> getAllSubRegions(Pageable pageable) {
        log.info("Fetching all delivery sub-regions with pagination");
        
        Page<DeliverySubRegion> subRegions = subRegionRepository.findAll(pageable);
        
        return subRegions.map(this::mapSubRegionToResponse);
    }

    @Override
    public Page<DeliverySubRegionResponse> searchSubRegions(String keyword, Pageable pageable) {
        log.info("Searching delivery sub-regions with keyword: {}", keyword);
        
        Page<DeliverySubRegion> subRegions = subRegionRepository.findBySubRegionNameContainingIgnoreCase(keyword, pageable);
        
        return subRegions.map(this::mapSubRegionToResponse);
    }

    //-------------------- Utility Operations --------------------//
    @Override
    public BigDecimal calculateDeliveryCost(UUID subRegionId) {
        log.info("Calculating delivery cost for sub-region ID: {}", subRegionId);
        
        DeliverySubRegion subRegion = subRegionRepository.findById(subRegionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-region not found with ID: " + subRegionId));
        
        DeliveryRegion region = subRegion.getRegion();
        DeliveryCountry country = region.getCountry();
        
        return country.getBaseDeliveryCost()
                .add(region.getAdditionalDeliveryCost())
                .add(subRegion.getAdditionalDeliveryCost());
    }

    @Override
    public BigDecimal calculateDeliveryCost(UUID countryId, UUID regionId, UUID subRegionId) {
        log.info("Calculating delivery cost for country ID: {}, region ID: {}, sub-region ID: {}", 
                countryId, regionId, subRegionId);
        
        DeliveryCountry country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + countryId));
        
        DeliveryRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with ID: " + regionId));
        
        // Check if region belongs to country
        if (!region.getCountry().getCountryId().equals(countryId)) {
            throw new IllegalArgumentException("Region does not belong to the specified country");
        }
        
        DeliverySubRegion subRegion = subRegionRepository.findById(subRegionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-region not found with ID: " + subRegionId));
        
        // Check if sub-region belongs to region
        if (!subRegion.getRegion().getRegionId().equals(regionId)) {
            throw new IllegalArgumentException("Sub-region does not belong to the specified region");
        }
        
        return country.getBaseDeliveryCost()
                .add(region.getAdditionalDeliveryCost())
                .add(subRegion.getAdditionalDeliveryCost());
    }

    //-------------------- Mapping Methods --------------------//
    private DeliveryCountryResponse mapCountryToResponse(DeliveryCountry country) {
        // Map regions if needed, otherwise set to empty list to avoid recursion
        List<DeliveryRegionResponse> regionResponses = Collections.emptyList();
        
        return DeliveryCountryResponse.builder()
                .countryId(country.getCountryId())
                .countryName(country.getCountryName())
                .countryCode(country.getCountryCode())
                .baseDeliveryCost(country.getBaseDeliveryCost())
                .isActive(country.getIsActive())
                .createdAt(country.getCreatedAt())
                .updatedAt(country.getUpdatedAt())
                .regions(regionResponses)
                .build();
    }
    
    private DeliveryRegionResponse mapRegionToResponse(DeliveryRegion region) {
        // Map sub-regions if needed, otherwise set to empty list to avoid recursion
        List<DeliverySubRegionResponse> subRegionResponses = Collections.emptyList();
        
        return DeliveryRegionResponse.builder()
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .additionalDeliveryCost(region.getAdditionalDeliveryCost())
                .countryId(region.getCountry().getCountryId())
                .countryName(region.getCountry().getCountryName())
                .isActive(region.getIsActive())
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .subRegions(subRegionResponses)
                .build();
    }
    
    private DeliverySubRegionResponse mapSubRegionToResponse(DeliverySubRegion subRegion) {
        DeliveryRegion region = subRegion.getRegion();
        DeliveryCountry country = region.getCountry();
        
        // Calculate total delivery cost
        BigDecimal totalDeliveryCost = country.getBaseDeliveryCost()
                .add(region.getAdditionalDeliveryCost())
                .add(subRegion.getAdditionalDeliveryCost());
        
        return DeliverySubRegionResponse.builder()
                .subRegionId(subRegion.getSubRegionId())
                .subRegionName(subRegion.getSubRegionName())
                .additionalDeliveryCost(subRegion.getAdditionalDeliveryCost())
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .countryId(country.getCountryId())
                .countryName(country.getCountryName())
                .totalDeliveryCost(totalDeliveryCost)
                .isActive(subRegion.getIsActive())
                .createdAt(subRegion.getCreatedAt())
                .updatedAt(subRegion.getUpdatedAt())
                .build();
    }
} 