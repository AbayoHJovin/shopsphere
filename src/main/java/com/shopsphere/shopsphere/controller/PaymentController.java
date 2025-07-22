package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.CardPaymentRequest;
import com.shopsphere.shopsphere.dto.request.MomoPaymentRequest;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/card")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<?> processCardPayment(
            @Valid @RequestBody CardPaymentRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Processing card payment for order: {}", request.getOrderId());
            PaymentResponse response = paymentService.processCardPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error processing card payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    
    @PostMapping("/momo")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<?> processMomoPayment(
            @Valid @RequestBody MomoPaymentRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Processing MTN Mobile Money payment for order: {}", request.getOrderId());
            PaymentResponse response = paymentService.processMomoPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error processing MTN Mobile Money payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPayment(
            @PathVariable UUID paymentId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching payment with ID: {}", paymentId);
            PaymentResponse response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentsByOrder(
            @PathVariable UUID orderId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching payments for order with ID: {}", orderId);
            List<PaymentResponse> responses = paymentService.getPaymentsByOrder(orderId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching payments for order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'COWORKER')")
    public ResponseEntity<?> refundPayment(
            @PathVariable UUID paymentId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Processing refund for payment with ID: {}", paymentId);
            PaymentResponse response = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing refund", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    
    @GetMapping("/order/{orderId}/validate")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<?> validatePaymentStatus(
            @PathVariable UUID orderId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Validating payment status for order with ID: {}", orderId);
            boolean isValid = paymentService.validatePaymentStatus(orderId);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validating payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
} 