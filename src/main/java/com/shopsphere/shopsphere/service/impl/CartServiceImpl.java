package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.CartItemRequest;
import com.shopsphere.shopsphere.dto.response.CartItemResponse;
import com.shopsphere.shopsphere.dto.response.CartResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Cart;
import com.shopsphere.shopsphere.models.CartProduct;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.repository.CartProductRepository;
import com.shopsphere.shopsphere.repository.CartRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.repository.UserRepository;
import com.shopsphere.shopsphere.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart(UUID userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = getOrCreateCart(user);
        List<CartProduct> allCartProducts = cartProductRepository.findByCart(cart);
        
        // Calculate total items and subtotal before pagination
        int totalItems = allCartProducts.size();
        BigDecimal subtotal = calculateSubtotal(allCartProducts);
        
        // Apply pagination
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allCartProducts.size());
        
        List<CartProduct> pageContent = start < end 
                ? allCartProducts.subList(start, end) 
                : new ArrayList<>();
                
        Page<CartProduct> cartProductsPage = new PageImpl<>(
                pageContent, pageable, allCartProducts.size());
        
        // Map to response DTOs
        List<CartItemResponse> cartItemResponses = cartProductsPage.getContent()
                .stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());
        
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(userId)
                .items(cartItemResponses)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .totalPages(cartProductsPage.getTotalPages())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(UUID userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check product stock
        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        
        Cart cart = getOrCreateCart(user);
        
        // Check if product already exists in cart
        Optional<CartProduct> existingCartProduct = cartProductRepository.findByCartAndProduct(cart, product);
        
        if (existingCartProduct.isPresent()) {
            // Update existing cart item
            CartProduct cartProduct = existingCartProduct.get();
            int newQuantity = cartProduct.getQuantity() + request.getQuantity();
            
            // Check product stock again with new total quantity
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Not enough stock available");
            }
            
            cartProduct.setQuantity(newQuantity);
            cartProductRepository.save(cartProduct);
        } else {
            // Add new item to cart
            CartProduct cartProduct = CartProduct.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartProductRepository.save(cartProduct);
        }
        
        // Return updated cart (first page)
        return getCart(userId, 0, 10);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID productId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Cart cart = getOrCreateCart(user);
        
        CartProduct cartProduct = cartProductRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Check if requested quantity is valid
        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        
        // Update quantity
        cartProduct.setQuantity(request.getQuantity());
        cartProductRepository.save(cartProduct);
        
        // Return updated cart (first page)
        return getCart(userId, 0, 10);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(UUID userId, UUID productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Cart cart = getOrCreateCart(user);
        
        cartProductRepository.deleteByCartAndProduct(cart, product);
        
        // Return updated cart (first page)
        return getCart(userId, 0, 10);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = getOrCreateCart(user);
        
        List<CartProduct> cartProducts = cartProductRepository.findByCart(cart);
        cartProductRepository.deleteAll(cartProducts);
    }

    @Override
    public int getCartItemsCount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = cartRepository.findByOwner(user).orElse(null);
        
        if (cart == null) {
            return 0;
        }
        
        List<CartProduct> cartProducts = cartProductRepository.findByCart(cart);
        return cartProducts.size();
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByOwner(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .owner(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartItemResponse mapToCartItemResponse(CartProduct cartProduct) {
        Product product = cartProduct.getProduct();
        String imageUrl = product.getImages() != null && !product.getImages().isEmpty() ?
                product.getImages().get(0).getImageUrl() : null;
        
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(cartProduct.getQuantity()));
        
        return CartItemResponse.builder()
                .id(cartProduct.getId())
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .previousPrice(product.getPreviousPrice())
                .imageUrl(imageUrl)
                .quantity(cartProduct.getQuantity())
                .stock(product.getStock())
                .totalPrice(totalPrice)
                .averageRating(product.getAverageRating())
                .ratingCount(product.getRatingCount())
                .build();
    }

    private BigDecimal calculateSubtotal(List<CartProduct> cartProducts) {
        return cartProducts.stream()
                .map(cartProduct -> 
                    cartProduct.getProduct().getPrice().multiply(BigDecimal.valueOf(cartProduct.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 