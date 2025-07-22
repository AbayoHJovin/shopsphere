package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.CartItemRequest;
import com.shopsphere.shopsphere.dto.response.CartResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        CartResponse response = cartService.getCart(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        CartResponse response = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable UUID productId,
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        CartResponse response = cartService.updateCartItem(userId, productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable UUID productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        CartResponse response = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        cartService.clearCart(userId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Cart cleared successfully")
                .success(true)
                .build());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        int count = cartService.getCartItemsCount(userId);
        return ResponseEntity.ok(count);
    }
} 