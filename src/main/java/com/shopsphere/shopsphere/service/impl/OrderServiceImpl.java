package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.CardPaymentRequest;
import com.shopsphere.shopsphere.dto.request.MomoPaymentRequest;
import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.request.OrderFilterRequest;
import com.shopsphere.shopsphere.dto.request.OrderItemRequest;
import com.shopsphere.shopsphere.dto.request.OrderProveDeliveryRequest;
import com.shopsphere.shopsphere.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.shopsphere.dto.request.QrScanRequest;
import com.shopsphere.shopsphere.dto.response.OrderItemResponse;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import com.shopsphere.shopsphere.dto.response.OrderTransactionResponse;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.dto.response.UserSummaryResponse;
import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.enums.OrderStatus;
import com.shopsphere.shopsphere.enums.PaymentStatus;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Discount;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.OrderItem;
import com.shopsphere.shopsphere.models.OrderTransaction;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.ProductImage;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.repository.OrderItemRepository;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.OrderTransactionRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.repository.UserRepository;
import com.shopsphere.shopsphere.service.OrderService;
import com.shopsphere.shopsphere.service.PaymentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTransactionRepository orderTransactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public OrderResponse processOrderWithPayment(OrderCreateRequest request, String userEmail) {
        log.info("Processing order with payment for user: {}", userEmail);
        
        // Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validate products and verify pricing
        validateOrderItems(request.getItems());
        
        // Process payment based on payment method
        PaymentResponse paymentResponse;
        try {
            if (request.getPaymentMethod().getType().equals("credit_card")) {
                // Process credit card payment
                CardPaymentRequest paymentRequest = buildCardPaymentRequest(request, userEmail);
                paymentResponse = paymentService.processCardPayment(paymentRequest);
            } else if (request.getPaymentMethod().getType().equals("mtn_momo")) {
                // Process mobile money payment
                MomoPaymentRequest paymentRequest = buildMomoPaymentRequest(request, userEmail);
                paymentResponse = paymentService.processMomoPayment(paymentRequest);
            } else {
                throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod().getType());
            }
            
            if (paymentResponse.getStatus() != PaymentStatus.COMPLETED && paymentResponse.getStatus() != PaymentStatus.PENDING) {
                throw new IllegalStateException("Payment failed: " + paymentResponse.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Payment processing error", e);
            throw new IllegalStateException("Payment processing failed: " + e.getMessage());
        }
        
        // Create order after successful payment
        OrderResponse orderResponse = createOrder(request, userEmail);
        
        return orderResponse;
    }

    @Override
    @Transactional
    public OrderResponse processGuestOrderWithPayment(OrderCreateRequest request) {
        log.info("Processing guest order with payment");
        
        // Validate products and verify pricing
        validateOrderItems(request.getItems());
        
        // Process payment based on payment method
        PaymentResponse paymentResponse;
        try {
            if (request.getPaymentMethod().getType().equals("credit_card")) {
                // Process credit card payment
                CardPaymentRequest paymentRequest = buildCardPaymentRequest(request, request.getEmail());
                paymentResponse = paymentService.processCardPayment(paymentRequest);
            } else if (request.getPaymentMethod().getType().equals("mtn_momo")) {
                // Process mobile money payment
                MomoPaymentRequest paymentRequest = buildMomoPaymentRequest(request, request.getEmail());
                paymentResponse = paymentService.processMomoPayment(paymentRequest);
            } else {
                throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod().getType());
            }
            
            if (paymentResponse.getStatus() != PaymentStatus.COMPLETED && paymentResponse.getStatus() != PaymentStatus.PENDING) {
                throw new IllegalStateException("Payment failed: " + paymentResponse.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Payment processing error", e);
            throw new IllegalStateException("Payment processing failed: " + e.getMessage());
        }
        
        // Create guest order after successful payment
        OrderResponse orderResponse = createGuestOrder(request);
        
        return orderResponse;
    }

    private void validateOrderItems(List<OrderItemRequest> items) {
        log.info("Validating order items and prices");
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        
        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));
            
            // Check if product has enough stock
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }
            
            // Verify price is correct
            BigDecimal currentPrice = getCurrentProductPrice(product);
            if (item.getPrice().compareTo(currentPrice) != 0) {
                throw new IllegalArgumentException("Price mismatch for product: " + product.getName() + 
                        ". Expected: " + currentPrice + ", Got: " + item.getPrice());
            }
            
            // Calculate subtotal
            calculatedTotal = calculatedTotal.add(currentPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
    }
    
    private BigDecimal getCurrentProductPrice(Product product) {
        // Check if there are any active discounts
        Optional<Discount> activeDiscount = product.getDiscounts().stream()
                .filter(Discount::isActive)
                .findFirst();
        
        if (activeDiscount.isPresent()) {
            // Apply discount
            BigDecimal discountAmount = product.getPrice()
                    .multiply(activeDiscount.get().getPercentage())
                    .divide(BigDecimal.valueOf(100));
            return product.getPrice().subtract(discountAmount);
        }
        
        // Return regular price
        return product.getPrice();
    }
    
    private CardPaymentRequest buildCardPaymentRequest(OrderCreateRequest request, String customerEmail) {
        return CardPaymentRequest.builder()
                .paymentMethodId(request.getPaymentMethod().getCardNumber())
                .amount(request.getTotalAmount())
                .currency("USD")
                .customerEmail(customerEmail)
                .description("Payment for order")
                .receiptEmail(customerEmail)
                .build();
    }
    
    private MomoPaymentRequest buildMomoPaymentRequest(OrderCreateRequest request, String customerEmail) {
        return MomoPaymentRequest.builder()
                .amount(request.getTotalAmount())
                .mobileNumber(request.getPaymentMethod().getMobileNumber())
                .currency("EUR") // MTN MoMo uses EUR as currency
                .customerEmail(customerEmail)
                .description("Payment for order")
                .orderId(null) // Will be set after order creation
                .build();
    }
    
    @Override
    public OrderResponse createOrder(OrderCreateRequest request, String userEmail) {
        log.info("Creating order for user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Create order
        Order order = buildOrderFromRequest(request, user);
        
        // Generate unique order code
        String uniqueOrderCode = UUID.randomUUID().toString();
        order.setOrderCode(passwordEncoder.encode(uniqueOrderCode));
        
        // Save order to get ID
        Order savedOrder = orderRepository.save(order);
        
        // Create order items
        List<OrderItem> orderItems = createOrderItems(request.getItems(), savedOrder);
        
        // Update product stock
        updateProductStock(orderItems);
        
        // Create transaction (assuming payment is successful for now)
        OrderTransaction transaction = createTransaction(savedOrder, request.getTotalAmount());
        
        // Refresh order to get all associations
        Order refreshedOrder = orderRepository.findById(savedOrder.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Return the unhashed order code to the frontend
        OrderResponse response = mapOrderToResponse(refreshedOrder, true);
        response.setOrderCode(uniqueOrderCode); // Send unhashed code for QR generation
        
        return response;
    }

    @Override
    @Transactional
    public OrderResponse createGuestOrder(OrderCreateRequest request) {
        
        // Generate unique order code
        String uniqueOrderCode = UUID.randomUUID().toString();
        
        // Create order without user
        Order order = buildOrderFromRequest(request, null);
        
        // Store hashed code in the database
        order.setOrderCode(passwordEncoder.encode(uniqueOrderCode));
        
        // Save order to get ID
        Order savedOrder = orderRepository.save(order);
        
        // Create order items
        List<OrderItem> orderItems = createOrderItems(request.getItems(), savedOrder);
        
        // Update product stock
        updateProductStock(orderItems);
        
        // Create transaction (assuming payment is successful for now)
        OrderTransaction transaction = createTransaction(savedOrder, request.getTotalAmount());
        
        // Refresh order to get all associations
        Order refreshedOrder = orderRepository.findById(savedOrder.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Return the unhashed order code to the frontend
        OrderResponse response = mapOrderToResponse(refreshedOrder, true);
        response.setOrderCode(uniqueOrderCode); // Send unhashed code for QR generation
        
        return response;
    }

    @Override
    public OrderResponse getOrderById(UUID orderId, String userEmail) {
        log.info("Fetching order with ID: {} for user: {}", orderId, userEmail);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user has permission to view this order
        boolean isAdmin = user.getRole() == Role.ADMIN || user.getRole() == Role.CO_WORKER;
        boolean isOwner = order.getUser() != null && order.getUser().getUserId().equals(user.getUserId());
        
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to view this order");
        }
        
        return mapOrderToResponse(order, isAdmin || isOwner);
    }

    @Override
    public OrderResponse getOrderByCode(String orderCode) {
        log.info("Fetching order with code: {}", orderCode);
        
        // Find orders and check if the provided code matches
        List<Order> orders = orderRepository.findAll();
        
        for (Order order : orders) {
            if (passwordEncoder.matches(orderCode, order.getOrderCode())) {
                OrderResponse response = mapOrderToResponse(order, true);
                response.setOrderCode(orderCode); // Send unhashed code for QR display/regeneration
                return response;
            }
        }
        
        throw new ResourceNotFoundException("Order not found or code is incorrect");
    }

    @Override
    public Page<OrderResponse> getCurrentUserOrders(String userEmail, Pageable pageable) {
        log.info("Fetching orders for user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<Order> orders = orderRepository.findByUser(user, pageable);
        
        return orders.map(order -> mapOrderToResponse(order, true));
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination");
        
        Page<Order> orders = orderRepository.findAll(pageable);
        
        return orders.map(order -> mapOrderToResponse(order, true));
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        log.info("Updating status of order with ID: {} to {}", orderId, request.getOrderStatus());
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Check if order is already delivered (after QR scan)
        if (order.isQrScanned() && order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot change status of an order that has been delivered via QR scan");
        }
        
        // Check if trying to set to DELIVERED with pending payment
        if (request.getOrderStatus() == OrderStatus.DELIVERED && 
            order.getPaymentStatus() == OrderPaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot deliver an order with pending payment");
        }
        
        order.setOrderStatus(request.getOrderStatus());
        
        // Update payment status if provided
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
            
            // Update transaction if exists
            if (order.getTransaction() != null) {
                order.getTransaction().setStatus(request.getPaymentStatus());
                orderTransactionRepository.save(order.getTransaction());
            }
        }
        
        // Update notes if provided
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        
        return mapOrderToResponse(updatedOrder, true);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID orderId, String userEmail) {
        log.info("Deleting order with ID: {} by user: {}", orderId, userEmail);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user has permission to delete this order
        boolean isAdmin = user.getRole() == Role.ADMIN || user.getRole() == Role.CO_WORKER;
        boolean isOwner = order.getUser() != null && order.getUser().getUserId().equals(user.getUserId());
        
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to delete this order");
        }
        
        // Restore product stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        orderRepository.delete(order);
    }

    @Override
    @Transactional
    public OrderResponse proveDelivery(UUID orderId, String userEmail) {
        log.info("Proving delivery for order with ID: {} by user: {}", orderId, userEmail);
        
        // This method is deprecated as we're now using QR code scanning
        // But keeping for backward compatibility
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user has permission to prove this order
        boolean isOwner = order.getUser() != null && order.getUser().getUserId().equals(user.getUserId());
        
        if (!isOwner) {
            throw new AccessDeniedException("You don't have permission to prove this order");
        }
        
        // Check if order status is DELIVERED
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order must be in DELIVERED status to be proven");
        }
        
        throw new IllegalStateException("This method is deprecated. Orders are now verified using QR code scanning");
    }

    @Override
    @Transactional
    public OrderResponse proveDeliveryByCode(OrderProveDeliveryRequest request) {
        log.info("Proving delivery for order with code: {}", request.getOrderCode());
        
        // This method is deprecated as we're now using QR code scanning
        // But keeping for backward compatibility
        
        throw new IllegalStateException("This method is deprecated. Orders are now verified using QR code scanning");
    }

    @Override
    @Transactional
    public OrderResponse verifyQrCodeAndDeliver(QrScanRequest request, String userEmail) {
        log.info("Verifying QR code: {}", request.getOrderCode());
        
        // Validate the admin/co-worker user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.CO_WORKER) {
            throw new AccessDeniedException("Only admins and co-workers can scan QR codes");
        }
        
        // Find the order by matching the code
        List<Order> orders = orderRepository.findAll();
        Order matchedOrder = null;
        
        for (Order order : orders) {
            if (passwordEncoder.matches(request.getOrderCode(), order.getOrderCode())) {
                matchedOrder = order;
                break;
            }
        }
        
        if (matchedOrder == null) {
            throw new ResourceNotFoundException("Order not found or QR code is invalid");
        }
        
        // Check if order has already been scanned
        if (matchedOrder.isQrScanned()) {
            throw new IllegalStateException("This QR code has already been scanned");
        }
        
        // Check payment status
        if (matchedOrder.getPaymentStatus() != OrderPaymentStatus.PAID) {
            throw new IllegalStateException("Cannot deliver an order that hasn't been paid");
        }
        
        // Update the order
        matchedOrder.setQrScanned(true);
        matchedOrder.setOrderStatus(OrderStatus.DELIVERED);
        matchedOrder.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(matchedOrder);
        
        // Return response without the raw order code
        return mapOrderToResponse(updatedOrder, false);
    }

    @Override
    public Page<OrderResponse> filterOrders(OrderFilterRequest filterRequest, String userEmail, Pageable pageable) {
        log.info("Filtering orders with criteria: {}", filterRequest);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = user.getRole() == Role.ADMIN || user.getRole() == Role.CO_WORKER;
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> orderRoot = query.from(Order.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // If not admin, only show user's orders
        if (!isAdmin) {
            predicates.add(cb.equal(orderRoot.get("user"), user));
        } else if (filterRequest.getCustomerEmail() != null && !filterRequest.getCustomerEmail().isEmpty()) {
            // Filter by customer email (admin only)
            Join<Order, User> userJoin = orderRoot.join("user");
            predicates.add(cb.equal(userJoin.get("email"), filterRequest.getCustomerEmail()));
        }
        
        // Date range filters
        if (filterRequest.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(orderRoot.get("orderDate"), filterRequest.getStartDate()));
        }
        
        if (filterRequest.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(orderRoot.get("orderDate"), filterRequest.getEndDate()));
        }
        
        // Status filters
        if (filterRequest.getOrderStatus() != null) {
            predicates.add(cb.equal(orderRoot.get("orderStatus"), filterRequest.getOrderStatus()));
        }
        
        if (filterRequest.getPaymentStatus() != null) {
            predicates.add(cb.equal(orderRoot.get("paymentStatus"), filterRequest.getPaymentStatus()));
        }
        
        // Address filters (admin only)
        if (isAdmin) {
            if (filterRequest.getCity() != null && !filterRequest.getCity().isEmpty()) {
                predicates.add(cb.equal(orderRoot.get("city"), filterRequest.getCity()));
            }
            
            if (filterRequest.getStateProvince() != null && !filterRequest.getStateProvince().isEmpty()) {
                predicates.add(cb.equal(orderRoot.get("stateProvince"), filterRequest.getStateProvince()));
            }
            
            if (filterRequest.getCountry() != null && !filterRequest.getCountry().isEmpty()) {
                predicates.add(cb.equal(orderRoot.get("country"), filterRequest.getCountry()));
            }
        }
        
        // QR scan filter
        if (filterRequest.getIsQrScanned() != null) {
            predicates.add(cb.equal(orderRoot.get("isQrScanned"), filterRequest.getIsQrScanned()));
        }
        
        // Order code filter (admin only)
        if (isAdmin && filterRequest.getOrderCode() != null && !filterRequest.getOrderCode().isEmpty()) {
            predicates.add(cb.like(orderRoot.get("orderCode"), "%" + filterRequest.getOrderCode() + "%"));
        }
        
        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        if (pageable.getSort().isSorted()) {
            query.orderBy(cb.desc(orderRoot.get("orderDate")));
        }
        
        // Execute query with pagination
        TypedQuery<Order> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Order> orders = typedQuery.getResultList();
        
        // Count total elements
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot));
        
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        Long count = entityManager.createQuery(countQuery).getSingleResult();
        
        // Map to response
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> mapOrderToResponse(order, isAdmin || (order.getUser() != null && order.getUser().getUserId().equals(user.getUserId()))))
                .collect(Collectors.toList());
        
        return new PageImpl<>(orderResponses, pageable, count);
    }
    
    // Helper methods
    private Order buildOrderFromRequest(OrderCreateRequest request, User user) {
        Order order = Order.builder()
                .user(user)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .stateProvince(request.getStateProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .notes(request.getNotes())
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(OrderPaymentStatus.PENDING)
                .totalAmount(request.getTotalAmount())
                .isQrScanned(false)
                .orderDate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return order;
    }
    
    private List<OrderItem> createOrderItems(List<OrderItemRequest> itemRequests, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemRequest.getProductId()));
            
            // Check if product has enough stock
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build();
            
            orderItems.add(orderItemRepository.save(orderItem));
        }
        
        return orderItems;
    }
    
    private void updateProductStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
    }
    
    private OrderTransaction createTransaction(Order order, BigDecimal amount) {
        OrderTransaction transaction = OrderTransaction.builder()
                .order(order)
                .amount(amount)
                .status(OrderPaymentStatus.PAID) // Assuming payment is successful for now
                .paymentMethod("Credit Card") // Default for now
                .transactionReference(UUID.randomUUID().toString())
                .transactionDate(LocalDateTime.now())
                .build();
        
        OrderTransaction savedTransaction = orderTransactionRepository.save(transaction);
        
        // Update order payment status
        order.setPaymentStatus(OrderPaymentStatus.PAID);
        order.setTransaction(savedTransaction);
        orderRepository.save(order);
        
        return savedTransaction;
    }
    
    private OrderResponse mapOrderToResponse(Order order, boolean includeOrderCode) {
        // Map order items
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    String imageUrl = null;
                    
                    // Get main image URL if available
                    Optional<ProductImage> mainImage = product.getImages().stream()
                            .filter(ProductImage::getIsMain)
                            .findFirst();
                    
                    if (mainImage.isPresent()) {
                        imageUrl = mainImage.get().getImageUrl();
                    }
                    
                    return OrderItemResponse.builder()
                            .orderItemId(item.getOrderItemId())
                            .productId(product.getProductId())
                            .productName(product.getName())
                            .productImageUrl(imageUrl)
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build();
                })
                .collect(Collectors.toList());
        
        // Map transaction
        OrderTransactionResponse transactionResponse = null;
        if (order.getTransaction() != null) {
            OrderTransaction transaction = order.getTransaction();
            transactionResponse = OrderTransactionResponse.builder()
                    .transactionId(transaction.getTransactionId())
                    .paymentMethod(transaction.getPaymentMethod())
                    .transactionReference(transaction.getTransactionReference())
                    .amount(transaction.getAmount())
                    .status(transaction.getStatus())
                    .transactionDate(transaction.getTransactionDate())
                    .build();
        }
        
        // Map user
        UserSummaryResponse userResponse = null;
        if (order.getUser() != null) {
            User user = order.getUser();
            userResponse = UserSummaryResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .profilePictureUrl(user.getProfilePicture())
                    .build();
        }
        
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderCode(includeOrderCode ? order.getOrderCode() : null)
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderDate(order.getOrderDate())
                .updatedAt(order.getUpdatedAt())
                .totalAmount(order.getTotalAmount())
                .shippingCost(order.getShippingCost())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .isQrScanned(order.isQrScanned())
                .user(userResponse)
                .email(order.getEmail())
                .firstName(order.getFirstName())
                .lastName(order.getLastName())
                .phoneNumber(order.getPhoneNumber())
                .streetAddress(order.getStreetAddress())
                .city(order.getCity())
                .stateProvince(order.getStateProvince())
                .postalCode(order.getPostalCode())
                .country(order.getCountry())
                .notes(order.getNotes())
                .items(itemResponses)
                .transaction(transactionResponse)
                .build();
    }
} 