package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.OrderCreateRequest;
import com.shopsphere.shopsphere.dto.request.OrderFilterRequest;
import com.shopsphere.shopsphere.dto.request.OrderItemRequest;
import com.shopsphere.shopsphere.dto.request.OrderProveDeliveryRequest;
import com.shopsphere.shopsphere.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.shopsphere.dto.response.OrderItemResponse;
import com.shopsphere.shopsphere.dto.response.OrderResponse;
import com.shopsphere.shopsphere.dto.response.OrderTransactionResponse;
import com.shopsphere.shopsphere.dto.response.UserSummaryResponse;
import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import com.shopsphere.shopsphere.enums.OrderStatus;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
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
    private final PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, String userEmail) {
        log.info("Creating order for user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Create order
        Order order = buildOrderFromRequest(request, user);
        
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
        
        return mapOrderToResponse(refreshedOrder, true);
    }

    @Override
    @Transactional
    public OrderResponse createGuestOrder(OrderCreateRequest request) {
        log.info("Creating guest order");
        
        // Validate order code
        if (request.getOrderCode() == null || request.getOrderCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Order code is required for guest orders");
        }
        
        // Check if order code already exists
        if (orderRepository.existsByOrderCode(request.getOrderCode())) {
            throw new IllegalArgumentException("Order code already exists. Please choose a different one.");
        }
        
        // Create order without user
        Order order = buildOrderFromRequest(request, null);
        
        // Hash the order code for security
        order.setOrderCode(passwordEncoder.encode(request.getOrderCode()));
        
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
        
        return mapOrderToResponse(refreshedOrder, false);
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
        List<Order> guestOrders = orderRepository.findByUserIsNull();
        
        for (Order order : guestOrders) {
            if (passwordEncoder.matches(orderCode, order.getOrderCode())) {
                return mapOrderToResponse(order, true);
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
        
        // Check if order is already proven as delivered
        if (order.getOrderStatus() == OrderStatus.PROVEN_DELIVERED) {
            throw new IllegalStateException("Cannot change status of an order that has been proven delivered");
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
        
        order.setOrderStatus(OrderStatus.PROVEN_DELIVERED);
        order.setHasUserProven(true);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        return mapOrderToResponse(updatedOrder, true);
    }

    @Override
    @Transactional
    public OrderResponse proveDeliveryByCode(OrderProveDeliveryRequest request) {
        log.info("Proving delivery for order with code: {}", request.getOrderCode());
        
        // Find orders and check if the provided code matches
        List<Order> guestOrders = orderRepository.findByUserIsNull();
        Order matchedOrder = null;
        
        for (Order order : guestOrders) {
            if (passwordEncoder.matches(request.getOrderCode(), order.getOrderCode())) {
                matchedOrder = order;
                break;
            }
        }
        
        if (matchedOrder == null) {
            throw new ResourceNotFoundException("Order not found or code is incorrect");
        }
        
        // Check if order status is DELIVERED
        if (matchedOrder.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order must be in DELIVERED status to be proven");
        }
        
        matchedOrder.setOrderStatus(OrderStatus.PROVEN_DELIVERED);
        matchedOrder.setHasUserProven(true);
        
        if (request.getFeedback() != null && !request.getFeedback().trim().isEmpty()) {
            matchedOrder.setNotes(matchedOrder.getNotes() + "\nDelivery Feedback: " + request.getFeedback());
        }
        
        matchedOrder.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(matchedOrder);
        
        return mapOrderToResponse(updatedOrder, true);
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
        
        // Delivery proof filter
        if (filterRequest.getHasUserProven() != null) {
            predicates.add(cb.equal(orderRoot.get("hasUserProven"), filterRequest.getHasUserProven()));
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
                .hasUserProven(false)
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
                .hasUserProven(order.isHasUserProven())
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