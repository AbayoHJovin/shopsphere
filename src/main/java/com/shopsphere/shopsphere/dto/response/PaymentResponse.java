package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private UUID paymentId;
    
    private UUID orderId;
    
    private String transactionId;
    
    private String paymentMethod;
    
    private BigDecimal amount;
    
    private String currency;
    
    private PaymentStatus status;
    
    private String errorMessage;
    
    private LocalDateTime createdAt;
    
    private String receiptUrl;
    
    private Map<String, Object> additionalData;
} 