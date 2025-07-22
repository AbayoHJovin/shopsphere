package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.CardPaymentRequest;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.enums.PaymentStatus;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.Payment;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    public PaymentResponse processPayment(CardPaymentRequest request) {
        try {
            log.info("Processing card payment for order: {}", request.getOrderId());
            
            // Get order
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));
            
            // Convert amount to cents (Stripe requires amounts in smallest currency unit)
            Long amountInCents = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            
            // Create or get customer
            Customer customer = getOrCreateCustomer(request.getCustomerEmail());
            
            // Create payment intent
            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", request.getOrderId().toString());
            
            // Build payment intent params
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setCustomer(customer.getId())
                    .setPaymentMethod(request.getPaymentMethodId())
                    .setConfirm(true)
                    .setDescription(request.getDescription() != null ? 
                            request.getDescription() : 
                            "Payment for order #" + order.getOrderCode())
                    .putAllMetadata(metadata);
            
            // Add receipt email if provided
            if (request.getReceiptEmail() != null) {
                paramsBuilder.setReceiptEmail(request.getReceiptEmail());
            }
            
            PaymentIntentCreateParams params = paramsBuilder.build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Create payment record
            Payment payment = Payment.builder()
                    .transactionId(paymentIntent.getId())
                    .paymentMethod("CARD")
                    .amount(request.getAmount())
                    .currency(request.getCurrency().toUpperCase())
                    .order(order)
                    .status(mapStripeStatus(paymentIntent.getStatus()))
                    .metadataJson(new JSONObject()
                            .put("paymentIntentId", paymentIntent.getId())
                            .put("customerId", customer.getId())
                            .toString())
                    .receiptUrl(paymentIntent.getLatestCharge() != null ? 
                            paymentIntent.getLatestCharge() : null)
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            
            // Return payment response
            return mapToPaymentResponse(savedPayment);
        } catch (StripeException e) {
            log.error("Error processing Stripe payment", e);
            
            // Create failed payment record
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));
            
            Payment payment = Payment.builder()
                    .paymentMethod("CARD")
                    .amount(request.getAmount())
                    .currency(request.getCurrency().toUpperCase())
                    .order(order)
                    .status(PaymentStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            
            return mapToPaymentResponse(savedPayment);
        }
    }
    
    public PaymentResponse refundPayment(UUID paymentId) {
        try {
            log.info("Processing refund for payment ID: {}", paymentId);
            
            // Get payment
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
            
            // Check if payment can be refunded
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Payment cannot be refunded. Current status: " + payment.getStatus());
            }
            
            // Extract payment intent ID
            JSONObject metadata = new JSONObject(payment.getMetadataJson());
            String paymentIntentId = metadata.getString("paymentIntentId");
            
            // Process refund via Stripe
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            
            Refund refund = Refund.create(params);
            
            // Update payment record
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setMetadataJson(new JSONObject(payment.getMetadataJson())
                    .put("refundId", refund.getId())
                    .toString());
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            return mapToPaymentResponse(updatedPayment);
        } catch (StripeException e) {
            log.error("Error processing Stripe refund", e);
            throw new RuntimeException("Error processing refund: " + e.getMessage());
        }
    }
    
    private Customer getOrCreateCustomer(String email) throws StripeException {
        // Search for existing customers with this email
        Map<String, Object> customerSearchParams = new HashMap<>();
        customerSearchParams.put("query", "email:'" + email + "'");
        
        com.stripe.model.CustomerSearchResult customers = Customer.search(customerSearchParams);
        
        if (!customers.getData().isEmpty()) {
            return customers.getData().get(0);
        }
        
        // Create new customer if none exists
        CustomerCreateParams createParams = CustomerCreateParams.builder()
                .setEmail(email)
                .setDescription("Customer for " + email)
                .build();
        
        return Customer.create(createParams);
    }
    
    private PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "processing", "requires_action", "requires_confirmation", "requires_payment_method" -> PaymentStatus.PENDING;
            case "canceled" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.FAILED;
        };
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        Map<String, Object> additionalData = new HashMap<>();
        
        // Extract and add metadata if available
        if (payment.getMetadataJson() != null && !payment.getMetadataJson().isEmpty()) {
            try {
                JSONObject metadataJson = new JSONObject(payment.getMetadataJson());
                for (String key : metadataJson.keySet()) {
                    additionalData.put(key, metadataJson.get(key));
                }
            } catch (Exception e) {
                log.error("Error parsing payment metadata JSON", e);
            }
        }
        
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
                .additionalData(additionalData)
                .build();
    }
} 