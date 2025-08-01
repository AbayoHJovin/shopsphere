package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.ConfigurationResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.service.ConfigurationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Public controller for accessing store configurations
 * Accessible by anyone without authentication
 */
@RestController
@RequestMapping("/api/public/configuration")
@RequiredArgsConstructor
@Slf4j
public class PublicConfigurationController {

    private final ConfigurationService configurationService;

    /**
     * Get the current store configuration
     */
    @GetMapping
    public ResponseEntity<?> getCurrentConfiguration(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching current store configuration (public)");
            ConfigurationResponse response = configurationService.getCurrentConfiguration();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching current configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching configuration: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get just the business info (name, contact, location)
     */
    @GetMapping("/business-info")
    public ResponseEntity<?> getBusinessInfo(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching business information");
            ConfigurationResponse config = configurationService.getCurrentConfiguration();
            
            Map<String, Object> businessInfo = new HashMap<>();
            businessInfo.put("businessName", config.getBusinessName());
            businessInfo.put("businessPhone", config.getBusinessPhone());
            businessInfo.put("businessEmail", config.getBusinessEmail());
            businessInfo.put("streetAddress", config.getStreetAddress());
            businessInfo.put("city", config.getCity());
            businessInfo.put("stateProvince", config.getStateProvince());
            businessInfo.put("postalCode", config.getPostalCode());
            businessInfo.put("country", config.getCountry());
            businessInfo.put("businessHours", config.getBusinessHours());
            
            return ResponseEntity.ok(businessInfo);
        } catch (Exception e) {
            log.error("Error fetching business information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching business information: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get just the social media links
     */
    @GetMapping("/social-media")
    public ResponseEntity<?> getSocialMediaLinks(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching social media links");
            ConfigurationResponse config = configurationService.getCurrentConfiguration();
            
            Map<String, String> socialMedia = new HashMap<>();
            socialMedia.put("facebook", config.getFacebookUrl());
            socialMedia.put("instagram", config.getInstagramUrl());
            socialMedia.put("twitter", config.getTwitterUrl());
            socialMedia.put("linkedin", config.getLinkedinUrl());
            socialMedia.put("youtube", config.getYoutubeUrl());
            
            return ResponseEntity.ok(socialMedia);
        } catch (Exception e) {
            log.error("Error fetching social media links", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching social media links: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get the minimum purchase amount for free delivery
     */
    @GetMapping("/free-delivery-minimum")
    public ResponseEntity<?> getFreeDeliveryMinimum(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching minimum purchase amount for free delivery");
            BigDecimal minAmount = configurationService.getMinPurchaseForFreeDelivery();
            
            Map<String, Object> response = new HashMap<>();
            response.put("minPurchaseForFreeDelivery", minAmount);
            response.put("currencyCode", configurationService.getCurrencyCode());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching minimum purchase amount for free delivery", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching minimum purchase amount: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }
} 