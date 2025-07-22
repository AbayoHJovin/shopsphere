package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.OrderFilterRequest;
import com.shopsphere.shopsphere.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.shopsphere.dto.request.QrScanRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.OrderService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Admin: Fetching all orders with pagination");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderResponse> response = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching all orders: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterOrders(
            @ModelAttribute OrderFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Admin: Filtering orders with criteria: {}", filterRequest);
            String userEmail = authentication.getName();
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderResponse> response = orderService.filterOrders(filterRequest, userEmail, pageable);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error filtering orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error filtering orders: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Admin: Updating status of order with ID: {} to {}", orderId, request.getOrderStatus());
            OrderResponse response = orderService.updateOrderStatus(orderId, request);
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
            log.error("Error updating order status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating order status: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
    
    @PostMapping("/scan-qr")
    public ResponseEntity<?> scanQrCode(
            @Valid @RequestBody QrScanRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Admin/Co-worker: Scanning QR code to verify and deliver order");
            String userEmail = authentication.getName();
            
            OrderResponse response = orderService.verifyQrCodeAndDeliver(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Order not found or QR code invalid", e);
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
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error scanning QR code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error scanning QR code: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
} 