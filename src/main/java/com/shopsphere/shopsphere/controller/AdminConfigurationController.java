package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.ConfigurationRequest;
import com.shopsphere.shopsphere.dto.response.ConfigurationResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.ConfigurationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin controller for managing store configurations
 * Only accessible by users with ADMIN role
 */
@RestController
@RequestMapping("/api/admin/configurations")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigurationController {

    private final ConfigurationService configurationService;

    /**
     * Create a new configuration
     */
    @PostMapping
    public ResponseEntity<?> createConfiguration(
            @Valid @RequestBody ConfigurationRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new configuration");
            String username = authentication.getName();
            ConfigurationResponse response = configurationService.createConfiguration(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating configuration: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Update an existing configuration
     */
    @PutMapping("/{configId}")
    public ResponseEntity<?> updateConfiguration(
            @PathVariable UUID configId,
            @Valid @RequestBody ConfigurationRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating configuration with ID: {}", configId);
            String username = authentication.getName();
            ConfigurationResponse response = configurationService.updateConfiguration(configId, request, username);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Configuration not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating configuration: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get a specific configuration by ID
     */
    @GetMapping("/{configId}")
    public ResponseEntity<?> getConfigurationById(
            @PathVariable UUID configId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching configuration with ID: {}", configId);
            ConfigurationResponse response = configurationService.getConfigurationById(configId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Configuration not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage(), servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching configuration: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }

    /**
     * Get the current active configuration
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentConfiguration(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching current active configuration");
            ConfigurationResponse response = configurationService.getCurrentConfiguration();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching current configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching current configuration: " + e.getMessage(), servletRequest.getRequestURI()));
        }
    }
} 