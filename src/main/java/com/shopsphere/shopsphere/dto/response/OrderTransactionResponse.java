package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.OrderPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTransactionResponse {

    private UUID transactionId;
    private String paymentMethod;
    private String transactionReference;
    private BigDecimal amount;
    private OrderPaymentStatus status;
    private LocalDateTime transactionDate;
}