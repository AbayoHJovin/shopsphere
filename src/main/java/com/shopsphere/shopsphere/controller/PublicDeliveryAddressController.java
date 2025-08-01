package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.DeliveryCountryResponse;
import com.shopsphere.shopsphere.dto.response.DeliveryRegionResponse;
import com.shopsphere.shopsphere.dto.response.DeliverySubRegionResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.DeliveryAddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Public controller for accessing delivery addresses
 * This controller does not require authentication
 */
@RestController
@RequestMapping("/api/public/delivery")
@RequiredArgsConstructor
@Slf4j
public class PublicDeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    /**
     * Get all active delivery countries
     */
    @GetMapping("/countries")
    public ResponseEntity<?> getAllActiveCountries(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all active delivery countries");
            List<DeliveryCountryResponse> response = deliveryAddressService.getAllActiveCountries();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching countries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching countries: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get a specific delivery country by ID
     */
    @GetMapping("/countries/{countryId}")
    public ResponseEntity<?> getCountryById(
            @PathVariable UUID countryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching delivery country with ID: {}", countryId);
            DeliveryCountryResponse response = deliveryAddressService.getCountryById(countryId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Country not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching country", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching country: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get all active regions for a specific country
     */
    @GetMapping("/countries/{countryId}/regions")
    public ResponseEntity<?> getRegionsByCountry(
            @PathVariable UUID countryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching regions for country with ID: {}", countryId);
            List<DeliveryRegionResponse> response = deliveryAddressService.getRegionsByCountryId(countryId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Country not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching regions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching regions: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get a specific region by ID
     */
    @GetMapping("/regions/{regionId}")
    public ResponseEntity<?> getRegionById(
            @PathVariable UUID regionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching delivery region with ID: {}", regionId);
            DeliveryRegionResponse response = deliveryAddressService.getRegionById(regionId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get all active sub-regions for a specific region
     */
    @GetMapping("/regions/{regionId}/sub-regions")
    public ResponseEntity<?> getSubRegionsByRegion(
            @PathVariable UUID regionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching sub-regions for region with ID: {}", regionId);
            List<DeliverySubRegionResponse> response = deliveryAddressService.getSubRegionsByRegionId(regionId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching sub-regions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching sub-regions: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get a specific sub-region by ID
     */
    @GetMapping("/sub-regions/{subRegionId}")
    public ResponseEntity<?> getSubRegionById(
            @PathVariable UUID subRegionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching delivery sub-region with ID: {}", subRegionId);
            DeliverySubRegionResponse response = deliveryAddressService.getSubRegionById(subRegionId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Sub-region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching sub-region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching sub-region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Calculate delivery cost for a specific sub-region
     */
    @GetMapping("/cost/sub-regions/{subRegionId}")
    public ResponseEntity<?> calculateDeliveryCost(
            @PathVariable UUID subRegionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Calculating delivery cost for sub-region with ID: {}", subRegionId);
            BigDecimal deliveryCost = deliveryAddressService.calculateDeliveryCost(subRegionId);
            
            return ResponseEntity.ok(new DeliveryCostResponse(deliveryCost));
        } catch (ResourceNotFoundException e) {
            log.error("Sub-region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error calculating delivery cost", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error calculating delivery cost: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }
    
    /**
     * Simple response class for delivery cost
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DeliveryCostResponse {
        private BigDecimal deliveryCost;
    }
} 