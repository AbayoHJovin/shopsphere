package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.ConfigurationRequest;
import com.shopsphere.shopsphere.dto.response.ConfigurationResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Configuration;
import com.shopsphere.shopsphere.repository.ConfigurationRepository;
import com.shopsphere.shopsphere.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    
    @Override
    @Transactional
    public ConfigurationResponse createConfiguration(ConfigurationRequest request, String username) {
        log.info("Creating new configuration by user: {}", username);
        
        Configuration configuration = mapToEntity(request);
        configuration.setUpdatedBy(username);
        
        Configuration savedConfiguration = configurationRepository.save(configuration);
        
        return mapToResponse(savedConfiguration);
    }

    @Override
    @Transactional
    public ConfigurationResponse updateConfiguration(UUID configId, ConfigurationRequest request, String username) {
        log.info("Updating configuration with ID: {} by user: {}", configId, username);
        
        Configuration configuration = configurationRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with ID: " + configId));
        
        updateEntityFromRequest(configuration, request);
        configuration.setUpdatedBy(username);
        
        Configuration updatedConfiguration = configurationRepository.save(configuration);
        
        return mapToResponse(updatedConfiguration);
    }

    @Override
    public ConfigurationResponse getConfigurationById(UUID configId) {
        log.info("Fetching configuration with ID: {}", configId);
        
        Configuration configuration = configurationRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with ID: " + configId));
        
        return mapToResponse(configuration);
    }

    @Override
    public ConfigurationResponse getCurrentConfiguration() {
        log.info("Fetching current active configuration");
        
        Configuration configuration = configurationRepository.findLatestConfiguration()
                .orElseGet(() -> {
                    log.info("No configuration found, creating default configuration");
                    Configuration defaultConfig = new Configuration();
                    defaultConfig.setBusinessName("ShopSphere");
                    defaultConfig.setMinPurchaseForFreeDelivery(BigDecimal.valueOf(100));
                    defaultConfig.setCurrencyCode("USD");
                    return configurationRepository.save(defaultConfig);
                });
        
        return mapToResponse(configuration);
    }

    @Override
    public BigDecimal getMinPurchaseForFreeDelivery() {
        Configuration configuration = configurationRepository.findLatestConfiguration()
                .orElseGet(() -> {
                    Configuration defaultConfig = new Configuration();
                    defaultConfig.setBusinessName("ShopSphere");
                    defaultConfig.setMinPurchaseForFreeDelivery(BigDecimal.valueOf(100));
                    defaultConfig.setCurrencyCode("USD");
                    return configurationRepository.save(defaultConfig);
                });
        
        return configuration.getMinPurchaseForFreeDelivery();
    }

    @Override
    public String getCurrencyCode() {
        Configuration configuration = configurationRepository.findLatestConfiguration()
                .orElseGet(() -> {
                    Configuration defaultConfig = new Configuration();
                    defaultConfig.setBusinessName("ShopSphere");
                    defaultConfig.setMinPurchaseForFreeDelivery(BigDecimal.valueOf(100));
                    defaultConfig.setCurrencyCode("USD");
                    return configurationRepository.save(defaultConfig);
                });
        
        return configuration.getCurrencyCode();
    }
    
    /**
     * Map request DTO to entity
     */
    private Configuration mapToEntity(ConfigurationRequest request) {
        return Configuration.builder()
                .businessName(request.getBusinessName())
                .businessPhone(request.getBusinessPhone())
                .businessEmail(request.getBusinessEmail())
                .facebookUrl(request.getFacebookUrl())
                .instagramUrl(request.getInstagramUrl())
                .twitterUrl(request.getTwitterUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .youtubeUrl(request.getYoutubeUrl())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .stateProvince(request.getStateProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .minPurchaseForFreeDelivery(request.getMinPurchaseForFreeDelivery())
                .currencyCode(request.getCurrencyCode())
                .businessHours(request.getBusinessHours())
                .build();
    }
    
    /**
     * Update entity from request
     */
    private void updateEntityFromRequest(Configuration configuration, ConfigurationRequest request) {
        configuration.setBusinessName(request.getBusinessName());
        configuration.setBusinessPhone(request.getBusinessPhone());
        configuration.setBusinessEmail(request.getBusinessEmail());
        configuration.setFacebookUrl(request.getFacebookUrl());
        configuration.setInstagramUrl(request.getInstagramUrl());
        configuration.setTwitterUrl(request.getTwitterUrl());
        configuration.setLinkedinUrl(request.getLinkedinUrl());
        configuration.setYoutubeUrl(request.getYoutubeUrl());
        configuration.setStreetAddress(request.getStreetAddress());
        configuration.setCity(request.getCity());
        configuration.setStateProvince(request.getStateProvince());
        configuration.setPostalCode(request.getPostalCode());
        configuration.setCountry(request.getCountry());
        
        if (request.getMinPurchaseForFreeDelivery() != null) {
            configuration.setMinPurchaseForFreeDelivery(request.getMinPurchaseForFreeDelivery());
        }
        
        if (request.getCurrencyCode() != null) {
            configuration.setCurrencyCode(request.getCurrencyCode());
        }
        
        configuration.setBusinessHours(request.getBusinessHours());
    }
    
    /**
     * Map entity to response DTO
     */
    private ConfigurationResponse mapToResponse(Configuration configuration) {
        return ConfigurationResponse.builder()
                .configId(configuration.getConfigId())
                .businessName(configuration.getBusinessName())
                .businessPhone(configuration.getBusinessPhone())
                .businessEmail(configuration.getBusinessEmail())
                .facebookUrl(configuration.getFacebookUrl())
                .instagramUrl(configuration.getInstagramUrl())
                .twitterUrl(configuration.getTwitterUrl())
                .linkedinUrl(configuration.getLinkedinUrl())
                .youtubeUrl(configuration.getYoutubeUrl())
                .streetAddress(configuration.getStreetAddress())
                .city(configuration.getCity())
                .stateProvince(configuration.getStateProvince())
                .postalCode(configuration.getPostalCode())
                .country(configuration.getCountry())
                .minPurchaseForFreeDelivery(configuration.getMinPurchaseForFreeDelivery())
                .currencyCode(configuration.getCurrencyCode())
                .businessHours(configuration.getBusinessHours())
                .createdAt(configuration.getCreatedAt())
                .updatedAt(configuration.getUpdatedAt())
                .updatedBy(configuration.getUpdatedBy())
                .build();
    }
} 