package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.CardPaymentRequest;
import com.shopsphere.shopsphere.dto.request.MomoPaymentRequest;
import com.shopsphere.shopsphere.dto.response.PaymentResponse;
import com.shopsphere.shopsphere.models.Order;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    
    PaymentResponse processCardPayment(CardPaymentRequest request);
    
    PaymentResponse processMomoPayment(MomoPaymentRequest request);
    
    PaymentResponse getPaymentById(UUID paymentId);
    
    List<PaymentResponse> getPaymentsByOrder(UUID orderId);
    
    PaymentResponse refundPayment(UUID paymentId);
    
    boolean validatePaymentStatus(UUID orderId);
} 