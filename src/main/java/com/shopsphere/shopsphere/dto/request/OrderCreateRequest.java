package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {

    // Customer information
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    // Address information
    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State/Province is required")
    private String stateProvince;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    // Order information
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;

    private String orderCode; // Optional - for guest users

    private String notes;

    // Payment information
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;
    
    // Payment method details
    @NotNull(message = "Payment method is required")
    @Valid
    private PaymentMethodRequest paymentMethod;
}