package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.RatingCreateRequest;
import com.shopsphere.shopsphere.dto.response.RatingResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Order;
import com.shopsphere.shopsphere.models.OrderItem;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.Rating;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.repository.RatingRepository;
import com.shopsphere.shopsphere.repository.UserRepository;
import com.shopsphere.shopsphere.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public RatingResponse createRating(RatingCreateRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if user has already rated this product
        Optional<Rating> existingRating = ratingRepository.findByProductAndUser(product, user);
        if (existingRating.isPresent()) {
            throw new IllegalStateException("You have already rated this product. Please update your existing rating instead.");
        }

        // Check if user has purchased the product (for verified purchase badge)
        boolean hasVerifiedPurchase = checkIfUserHasPurchasedProduct(user, product);

        Rating rating = Rating.builder()
                .stars(request.getStars())
                .comment(request.getComment())
                .product(product)
                .user(user)
                .verifiedPurchase(hasVerifiedPurchase)
                .build();

        Rating savedRating = ratingRepository.save(rating);

        // Update product rating statistics
        updateProductRatingStatistics(product);

        return mapToRatingResponse(savedRating);
    }

    @Override
    public List<RatingResponse> getProductRatings(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return ratingRepository.findByProductOrderByCreatedAtDesc(product)
                .stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RatingResponse> getUserRatings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ratingRepository.findByUser(user)
                .stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RatingResponse updateRating(UUID ratingId, RatingCreateRequest request, UUID userId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        // Ensure user owns the rating
        if (!rating.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this rating");
        }

        // Update fields
        rating.setStars(request.getStars());
        rating.setComment(request.getComment());

        Rating updatedRating = ratingRepository.save(rating);

        // Update product rating statistics
        updateProductRatingStatistics(rating.getProduct());

        return mapToRatingResponse(updatedRating);
    }

    @Override
    @Transactional
    public void deleteRating(UUID ratingId, UUID userId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        // Ensure user owns the rating
        if (!rating.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this rating");
        }

        Product product = rating.getProduct();
        ratingRepository.delete(rating);

        // Update product rating statistics
        updateProductRatingStatistics(product);
    }

    @Override
    public Double getAverageRatingForProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        return ratingRepository.findAverageRatingByProduct(product);
    }

    private RatingResponse mapToRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .ratingId(rating.getRatingId())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .productId(rating.getProduct().getProductId())
                .productName(rating.getProduct().getName())
                .userId(rating.getUser().getUserId())
                .username(rating.getUser().getUsername())
                .userProfilePicture(rating.getUser().getProfilePicture())
                .createdAt(rating.getCreatedAt())
                .verifiedPurchase(rating.getVerifiedPurchase())
                .build();
    }

    @Transactional
    private void updateProductRatingStatistics(Product product) {
        Double averageRating = ratingRepository.findAverageRatingByProduct(product);
        Integer ratingCount = ratingRepository.countRatingsByProduct(product);
        
        product.setAverageRating(averageRating != null ? averageRating : 0.0);
        product.setRatingCount(ratingCount != null ? ratingCount : 0);
        
        productRepository.save(product);
    }

    private boolean checkIfUserHasPurchasedProduct(User user, Product product) {
        List<Order> userOrders = orderRepository.findByUser(user, null).getContent();
        
        return userOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getProductId().equals(product.getProductId()));
    }
} 