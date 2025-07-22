package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.CardPaymentRequest;
import com.shopsphere.shopsphere.dto.request.MomoPaymentRequest;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.enums.PaymentStatus;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.Payment;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.PaymentRepository;
import com.shopsphere.shopsphere.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeService stripeService;
    private final MtnMomoService mtnMomoService;
    
    @Override
    @Transactional
    public PaymentResponse processCardPayment(CardPaymentRequest request) {
        log.info("Processing card payment for order: {}", request.getOrderId());
        return stripeService.processPayment(request);
    }
    
    @Override
    @Transactional
    public PaymentResponse processMomoPayment(MomoPaymentRequest request) {
        log.info("Processing MTN Mobile Money payment for order: {}", request.getOrderId());
        return mtnMomoService.processPayment(request);
    }
    
    @Override
    public PaymentResponse getPaymentById(UUID paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        // Check if it's a MTN Mobile Money payment and update status if needed
        if (payment.getPaymentMethod().equals("MTN_MOMO")) {
            return mtnMomoService.checkPaymentStatus(paymentId);
        }
        
        return mapToPaymentResponse(payment);
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByOrder(UUID orderId) {
        log.info("Fetching payments for order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        List<Payment> payments = paymentRepository.findByOrder(order);
        
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId) {
        log.info("Processing refund for payment with ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        if (payment.getPaymentMethod().equals("CARD")) {
            return stripeService.refundPayment(paymentId);
        } else {
            throw new UnsupportedOperationException("Refund is not supported for payment method: " + payment.getPaymentMethod());
        }
    }
    
    @Override
    public boolean validatePaymentStatus(UUID orderId) {
        log.info("Validating payment status for order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        List<Payment> payments = paymentRepository.findByOrderAndStatus(order, PaymentStatus.COMPLETED);
        
        return !payments.isEmpty();
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .transactionId(payment.getTransactionId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .receiptUrl(payment.getReceiptUrl())
                .build();
    }
} 