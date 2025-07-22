package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.request.OrderFilterRequest;
import com.shopsphere.shopsphere.dto.request.OrderProveDeliveryRequest;
import com.shopsphere.shopsphere.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.shopsphere.dto.request.QrScanRequest;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    // Create order
    OrderResponse createOrder(OrderCreateRequest request, String userEmail);

    // Guest order creation
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