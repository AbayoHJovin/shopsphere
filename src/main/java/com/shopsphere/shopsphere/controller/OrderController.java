package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.request.OrderFilterRequest;
import com.shopsphere.shopsphere.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating order for authenticated user");
            String userEmail = authentication.getName();
            OrderResponse response = orderService.createOrder(request, userEmail);
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
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating order: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderById(
            @PathVariable UUID orderId,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching order with ID: {}", orderId);
            String userEmail = authentication.getName();
            OrderResponse response = orderService.getOrderById(orderId, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Order not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching order: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching orders for authenticated user");
            String userEmail = authentication.getName();

            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<OrderResponse> response = orderService.getCurrentUserOrders(userEmail, pageable);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching orders: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> filterOrders(
            @ModelAttribute OrderFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Filtering orders with criteria: {}", filterRequest);
            String userEmail = authentication.getName();

            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
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

    @DeleteMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteOrder(
            @PathVariable UUID orderId,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting order with ID: {}", orderId);
            String userEmail = authentication.getName();
            orderService.deleteOrder(orderId, userEmail);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Order deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Order not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting order: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PatchMapping("/{orderId}/prove-delivery")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> proveDelivery(
            @PathVariable UUID orderId,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Proving delivery for order with ID: {}", orderId);
            String userEmail = authentication.getName();
            OrderResponse response = orderService.proveDelivery(orderId, userEmail);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Order not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
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