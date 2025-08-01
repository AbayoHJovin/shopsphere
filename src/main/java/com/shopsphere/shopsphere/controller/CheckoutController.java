package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final OrderService orderService;
    
    /**
     * Get a list of available countries for shipping
     */
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        log.info("Fetching available countries for shipping");
        List<String> countries = Arrays.asList(
            "United States", "Canada", "United Kingdom", "France", "Germany",
            "Italy", "Spain", "Australia", "Japan", "China", "Brazil",
            "Mexico", "India", "South Africa", "Russia", "Netherlands",
            "Belgium", "Sweden", "Norway", "Denmark", "Finland", "Ireland",
            "New Zealand", "Singapore", "South Korea", "Taiwan", "Thailand",
            "Malaysia", "Indonesia", "Philippines", "Vietnam", "United Arab Emirates",
            "Saudi Arabia", "Qatar", "Kuwait", "Bahrain", "Oman", "Egypt",
            "Morocco", "Tunisia", "Kenya", "Nigeria", "Ghana"
        );
        return ResponseEntity.ok(countries);
    }

    /**
     * Process an order with payment for authenticated users
     * This endpoint handles both payment processing and order creation
     */
    @PostMapping("/complete")
    public ResponseEntity<?> processOrderWithPayment(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Processing order with payment for user: {}", 
                    authentication != null ? authentication.getName() : "anonymous");
            
            if (authentication != null && authentication.isAuthenticated()) {
                // Process order for authenticated user
                OrderResponse response = orderService.processOrderWithPayment(request, authentication.getName());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                // Process order for guest user
                OrderResponse response = orderService.processGuestOrderWithPayment(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, 
                            e.getMessage(), 
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, 
                            e.getMessage(), 
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error processing checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Payment failed: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }
    
    /**
     * Process a guest order with payment
     * This endpoint handles both payment processing and order creation for guest users
     */
    @PostMapping("/guest/complete")
    public ResponseEntity<?> processGuestOrderWithPayment(
            @Valid @RequestBody OrderCreateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Processing guest order with payment");
            OrderResponse response = orderService.processGuestOrderWithPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND, 
                            e.getMessage(), 
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, 
                            e.getMessage(), 
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error processing guest checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Payment failed: " + e.getMessage(), 
                            servletRequest.getRequestURI()));
        }
    }
} 