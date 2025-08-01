package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.request.OrderProveDeliveryRequest;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/orders")
@RequiredArgsConstructor
@Slf4j
public class GuestOrderController {

    private final OrderService orderService;

    /**
     * @deprecated Use {@link CheckoutController#processGuestOrderWithPayment} instead.
     * This endpoint does not support payment processing.
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<?> createGuestOrder(
            @Valid @RequestBody OrderCreateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating guest order using deprecated endpoint");
            log.warn("This endpoint is deprecated. Please use /api/checkout/guest/complete instead, which includes payment processing.");
            
            // Check if payment method is included
            if (request.getPaymentMethod() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                                "This endpoint does not support payment processing. Please use /api/checkout/guest/complete instead.",
                                servletRequest.getRequestURI()));
            }
            
            OrderResponse response = orderService.createGuestOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error creating guest order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating guest order: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/track")
    public ResponseEntity<?> trackOrderByCode(
            @RequestParam String orderCode,
            HttpServletRequest servletRequest) {
        try {
            log.info("Tracking order with code: {}", orderCode);
            OrderResponse response = orderService.getOrderByCode(orderCode);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Order not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error tracking order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error tracking order: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PostMapping("/prove-delivery")
    public ResponseEntity<?> proveDeliveryByCode(
            @Valid @RequestBody OrderProveDeliveryRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Proving delivery for order with code: {}", request.getOrderCode());
            OrderResponse response = orderService.proveDeliveryByCode(request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Order not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalStateException e) {
            log.error("Invalid state", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error proving delivery", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error proving delivery: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
} 