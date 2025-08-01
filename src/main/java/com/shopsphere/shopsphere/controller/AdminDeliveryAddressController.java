package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.DeliveryCountryRequest;
import com.shopsphere.shopsphere.dto.request.DeliveryRegionRequest;
import com.shopsphere.shopsphere.dto.request.DeliverySubRegionRequest;
import com.shopsphere.shopsphere.dto.response.DeliveryCountryResponse;
import com.shopsphere.shopsphere.dto.response.DeliveryRegionResponse;
import com.shopsphere.shopsphere.dto.response.DeliverySubRegionResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.DeliveryAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for managing delivery addresses (Countries, Regions, SubRegions)
 * This controller requires ADMIN role
 */
@RestController
@RequestMapping("/api/admin/delivery")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminDeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    // -------------------- Country Endpoints --------------------
    /**
     * Create a new delivery country
     */
    @PostMapping("/countries")
    public ResponseEntity<?> createCountry(
            @Valid @RequestBody DeliveryCountryRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new delivery country: {}", request.getCountryName());
            DeliveryCountryResponse response = deliveryAddressService.createCountry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error creating country", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating country: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Update an existing delivery country
     */
    @PutMapping("/countries/{countryId}")
    public ResponseEntity<?> updateCountry(
            @PathVariable UUID countryId,
            @Valid @RequestBody DeliveryCountryRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating delivery country with ID: {}", countryId);
            DeliveryCountryResponse response = deliveryAddressService.updateCountry(countryId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Country not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating country", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating country: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Delete a delivery country
     */
    @DeleteMapping("/countries/{countryId}")
    public ResponseEntity<?> deleteCountry(
            @PathVariable UUID countryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting delivery country with ID: {}", countryId);
            deliveryAddressService.deleteCountry(countryId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Country deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Country not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalStateException e) {
            log.error("Cannot delete country with regions", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting country", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting country: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get all countries with pagination
     */
    @GetMapping("/countries")
    public ResponseEntity<?> getAllCountries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "countryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all delivery countries");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DeliveryCountryResponse> response = deliveryAddressService.getAllCountries(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching countries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching countries: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Search countries by name
     */
    @GetMapping("/countries/search")
    public ResponseEntity<?> searchCountries(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Searching delivery countries with keyword: {}", keyword);
            Pageable pageable = PageRequest.of(page, size);
            
            Page<DeliveryCountryResponse> response = deliveryAddressService.searchCountries(keyword, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching countries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error searching countries: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    // -------------------- Region Endpoints --------------------
    /**
     * Create a new delivery region
     */
    @PostMapping("/regions")
    public ResponseEntity<?> createRegion(
            @Valid @RequestBody DeliveryRegionRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new delivery region: {}", request.getRegionName());
            DeliveryRegionResponse response = deliveryAddressService.createRegion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            log.error("Country not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error creating region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Update an existing delivery region
     */
    @PutMapping("/regions/{regionId}")
    public ResponseEntity<?> updateRegion(
            @PathVariable UUID regionId,
            @Valid @RequestBody DeliveryRegionRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating delivery region with ID: {}", regionId);
            DeliveryRegionResponse response = deliveryAddressService.updateRegion(regionId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Region or country not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Delete a delivery region
     */
    @DeleteMapping("/regions/{regionId}")
    public ResponseEntity<?> deleteRegion(
            @PathVariable UUID regionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting delivery region with ID: {}", regionId);
            deliveryAddressService.deleteRegion(regionId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Region deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalStateException e) {
            log.error("Cannot delete region with sub-regions", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get regions by country ID
     */
    @GetMapping("/countries/{countryId}/regions")
    public ResponseEntity<?> getRegionsByCountry(
            @PathVariable UUID countryId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching regions for country with ID: {}", countryId);
            var response = deliveryAddressService.getRegionsByCountryId(countryId);
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
     * Get all regions with pagination
     */
    @GetMapping("/regions")
    public ResponseEntity<?> getAllRegions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "regionName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all delivery regions");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DeliveryRegionResponse> response = deliveryAddressService.getAllRegions(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching regions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching regions: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    // -------------------- SubRegion Endpoints --------------------
    /**
     * Create a new delivery sub-region
     */
    @PostMapping("/sub-regions")
    public ResponseEntity<?> createSubRegion(
            @Valid @RequestBody DeliverySubRegionRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new delivery sub-region: {}", request.getSubRegionName());
            DeliverySubRegionResponse response = deliveryAddressService.createSubRegion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            log.error("Region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error creating sub-region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating sub-region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Update an existing delivery sub-region
     */
    @PutMapping("/sub-regions/{subRegionId}")
    public ResponseEntity<?> updateSubRegion(
            @PathVariable UUID subRegionId,
            @Valid @RequestBody DeliverySubRegionRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating delivery sub-region with ID: {}", subRegionId);
            DeliverySubRegionResponse response = deliveryAddressService.updateSubRegion(subRegionId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Sub-region or region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating sub-region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating sub-region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Delete a delivery sub-region
     */
    @DeleteMapping("/sub-regions/{subRegionId}")
    public ResponseEntity<?> deleteSubRegion(
            @PathVariable UUID subRegionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting delivery sub-region with ID: {}", subRegionId);
            deliveryAddressService.deleteSubRegion(subRegionId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Sub-region deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Sub-region not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting sub-region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting sub-region: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get sub-regions by region ID
     */
    @GetMapping("/regions/{regionId}/sub-regions")
    public ResponseEntity<?> getSubRegionsByRegion(
            @PathVariable UUID regionId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching sub-regions for region with ID: {}", regionId);
            var response = deliveryAddressService.getSubRegionsByRegionId(regionId);
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
     * Get all sub-regions with pagination
     */
    @GetMapping("/sub-regions")
    public ResponseEntity<?> getAllSubRegions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "subRegionName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all delivery sub-regions");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DeliverySubRegionResponse> response = deliveryAddressService.getAllSubRegions(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching sub-regions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching sub-regions: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }
} 