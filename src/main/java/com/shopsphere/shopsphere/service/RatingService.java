package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.RatingCreateRequest;
import com.shopsphere.shopsphere.dto.response.RatingResponse;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    
    RatingResponse createRating(RatingCreateRequest request, UUID userId);
    
    List<RatingResponse> getProductRatings(UUID productId);
    
    List<RatingResponse> getUserRatings(UUID userId);
    
    RatingResponse updateRating(UUID ratingId, RatingCreateRequest request, UUID userId);
    
    void deleteRating(UUID ratingId, UUID userId);
    
    Double getAverageRatingForProduct(UUID productId);
} 