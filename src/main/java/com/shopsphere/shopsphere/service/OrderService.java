package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.request.OrderFilterRequest;
import com.shopsphere.shopsphere.dto.request.OrderProveDeliveryRequest;
import com.shopsphere.shopsphere.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.shopsphere.dto.request.QrScanRequest;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    /**
     * Create and process an order with payment for authenticated user
     * This method handles both payment processing and order creation
     */
    OrderResponse processOrderWithPayment(OrderCreateRequest request, String userEmail);

    /**
     * Create and process an order with payment for guest user
     * This method handles both payment processing and order creation
     */
    OrderResponse processGuestOrderWithPayment(OrderCreateRequest request);
    
    // Create order (after successful payment)
    OrderResponse createOrder(OrderCreateRequest request, String userEmail);

    // Guest order creation (after successful payment)
    OrderResponse createGuestOrder(OrderCreateRequest request);

    // Get order by ID
    OrderResponse getOrderById(UUID orderId, String userEmail);

    // Get order by code (for guest users)
    OrderResponse getOrderByCode(String orderCode);

    // Get orders for current user
    Page<OrderResponse> getCurrentUserOrders(String userEmail, Pageable pageable);

    // Get all orders (admin/co-worker)
    Page<OrderResponse> getAllOrders(Pageable pageable);

    // Update order status (admin/co-worker)
    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);

    // Delete order
    void deleteOrder(UUID orderId, String userEmail);

    // Prove delivery (authenticated user)
    OrderResponse proveDelivery(UUID orderId, String userEmail);

    // Prove delivery (guest user)
    OrderResponse proveDeliveryByCode(OrderProveDeliveryRequest request);

    // Filter orders
    Page<OrderResponse> filterOrders(OrderFilterRequest filterRequest, String userEmail, Pageable pageable);
    
    // QR code scan verification (admin/co-worker)
    OrderResponse verifyQrCodeAndDeliver(QrScanRequest request, String userEmail);
}