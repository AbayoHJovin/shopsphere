package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.MomoPaymentRequest;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.enums.PaymentStatus;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.Payment;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MtnMomoService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OkHttpClient httpClient = new OkHttpClient();
    
    @Value("${mtn.momo.collection.primary-key}")
    private String primaryKey;
    
    @Value("${mtn.momo.collection.secondary-key}")
    private String secondaryKey;
    
    @Value("${mtn.momo.collection.user-id}")
    private String userId;
    
    @Value("${mtn.momo.collection.api-key}")
    private String apiKey;
    
    @Value("${mtn.momo.base-url}")
    private String baseUrl;
    
    public PaymentResponse processPayment(MomoPaymentRequest request) {
        try {
            log.info("Processing MTN Mobile Money payment for order: {}", request.getOrderId());
            
            // Get order
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));
            
            // Generate reference ID
            String referenceId = generateReferenceId();
            
            // Create token
            String token = getToken();
            
            // Initiate payment
            String transactionId = initiatePayment(token, request, referenceId);
            
            // Create payment record
            Payment payment = Payment.builder()
                    .transactionId(transactionId)
                    .paymentMethod("MTN_MOMO")
                    .amount(request.getAmount())
                    .currency(request.getCurrency().toUpperCase())
                    .order(order)
                    .status(PaymentStatus.PENDING) // Assuming payment is pending until confirmed
                    .metadataJson(new JSONObject()
                            .put("referenceId", referenceId)
                            .put("mobileNumber", request.getMobileNumber())
                            .toString())
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            
            // Return payment response
            return mapToPaymentResponse(savedPayment);
        } catch (Exception e) {
            log.error("Error processing MTN Mobile Money payment", e);
            
            // Create failed payment record
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));
            
            Payment payment = Payment.builder()
                    .paymentMethod("MTN_MOMO")
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
    
    public PaymentResponse checkPaymentStatus(UUID paymentId) {
        log.info("Checking payment status for payment ID: {}", paymentId);
        
        // Get payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        if (payment.getPaymentMethod().equals("MTN_MOMO")) {
            try {
                JSONObject metadata = new JSONObject(payment.getMetadataJson());
                String referenceId = metadata.getString("referenceId");
                
                // Get token
                String token = getToken();
                
                // Check payment status
                PaymentStatus status = checkMomoPaymentStatus(token, payment.getTransactionId(), referenceId);
                
                // Update payment status if different
                if (status != payment.getStatus()) {
                    payment.setStatus(status);
                    payment = paymentRepository.save(payment);
                }
            } catch (Exception e) {
                log.error("Error checking MTN Mobile Money payment status", e);
            }
        }
        
        return mapToPaymentResponse(payment);
    }
    
    private String getToken() throws IOException {
        String credentials = Base64.getEncoder().encodeToString((userId + ":" + apiKey).getBytes());
        
        Request request = new Request.Builder()
                .url(baseUrl + "/collection/token/")
                .addHeader("Authorization", "Basic " + credentials)
                .addHeader("Ocp-Apim-Subscription-Key", primaryKey)
                .post(RequestBody.create(new byte[0], null))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get token: " + response.code());
            }
            
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getString("access_token");
        }
    }
    
    private String initiatePayment(String token, MomoPaymentRequest request, String referenceId) throws IOException {
        // Create payment request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("amount", request.getAmount().toString());
        requestBody.put("currency", request.getCurrency());
        requestBody.put("externalId", request.getOrderId().toString());
        requestBody.put("payer", new JSONObject()
                .put("partyIdType", "MSISDN")
                .put("partyId", request.getMobileNumber().replace("+", "")));
        requestBody.put("payerMessage", request.getDescription() != null ? 
                request.getDescription() : 
                "Payment for order");
        requestBody.put("payeeNote", "Payment received");
        
        Request paymentRequest = new Request.Builder()
                .url(baseUrl + "/collection/v1_0/requesttopay")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-Reference-Id", referenceId)
                .addHeader("X-Target-Environment", "sandbox") // Use "production" for production environment
                .addHeader("Ocp-Apim-Subscription-Key", primaryKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();
        
        try (Response response = httpClient.newCall(paymentRequest).execute()) {
            if (response.code() != 202) {
                throw new IOException("Failed to initiate payment: " + response.code());
            }
            
            return referenceId;
        }
    }
    
    private PaymentStatus checkMomoPaymentStatus(String token, String transactionId, String referenceId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/collection/v1_0/requesttopay/" + referenceId)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-Target-Environment", "sandbox") // Use "production" for production environment
                .addHeader("Ocp-Apim-Subscription-Key", primaryKey)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return PaymentStatus.FAILED;
            }
            
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            String status = jsonResponse.getString("status");
            
            return switch (status.toUpperCase()) {
                case "SUCCESSFUL" -> PaymentStatus.COMPLETED;
                case "PENDING" -> PaymentStatus.PENDING;
                case "FAILED" -> PaymentStatus.FAILED;
                default -> PaymentStatus.FAILED;
            };
        }
    }
    
    private String generateReferenceId() {
        return UUID.randomUUID().toString();
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