package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.CartItemRequest;
import com.shopsphere.shopsphere.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    
    CartResponse getCart(UUID userId, int page, int size);
    
    CartResponse addItemToCart(UUID userId, CartItemRequest request);
    
    CartResponse updateCartItem(UUID userId, UUID productId, CartItemRequest request);
    
    CartResponse removeItemFromCart(UUID userId, UUID productId);
    
    void clearCart(UUID userId);
    
    int getCartItemsCount(UUID userId);
} 