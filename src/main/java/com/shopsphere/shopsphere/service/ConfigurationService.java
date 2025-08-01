package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.ConfigurationRequest;
import com.shopsphere.shopsphere.dto.response.ConfigurationResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface ConfigurationService {

    /**
     * Create a new configuration
     */
    ConfigurationResponse createConfiguration(ConfigurationRequest request, String username);
    
    /**
     * Update an existing configuration
     */
    ConfigurationResponse updateConfiguration(UUID configId, ConfigurationRequest request, String username);
    
    /**
     * Get a configuration by ID
     */
    ConfigurationResponse getConfigurationById(UUID configId);
    
    /**
     * Get the current active configuration (most recently updated)
     */
    ConfigurationResponse getCurrentConfiguration();
    
    /**
     * Get the minimum purchase amount for free delivery
     * Used in checkout to determine if delivery should be free
     */
    BigDecimal getMinPurchaseForFreeDelivery();
    
    /**
     * Get the current store currency code (e.g., USD, EUR)
     */
    String getCurrencyCode();
} 