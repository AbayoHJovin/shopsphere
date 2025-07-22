package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;

/**
 * Controller for public payment operations
 * This provides endpoints that don't require authentication
 */
@RestController
@RequestMapping("/api/public/payments")
@RequiredArgsConstructor
@Slf4j
public class PublicPaymentController {
    
    private final PaymentService paymentService;
    
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable UUID paymentId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Checking payment status for payment with ID: {}", paymentId);
            PaymentResponse response = paymentService.getPaymentById(paymentId);
            
            // Only return limited information for public access
            return ResponseEntity.ok(Map.of(
                    "paymentId", response.getPaymentId(),
                    "status", response.getStatus(),
                    "paymentMethod", response.getPaymentMethod()
            ));
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    
    @GetMapping("/order/{orderId}/validate")
    public ResponseEntity<?> validatePaymentStatus(
            @PathVariable UUID orderId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Public access: Validating payment status for order with ID: {}", orderId);
            boolean isValid = paymentService.validatePaymentStatus(orderId);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validating payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
} 