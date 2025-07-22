package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.RatingResponse;
import com.shopsphere.shopsphere.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/ratings")
@RequiredArgsConstructor
public class PublicRatingController {

    private final RatingService ratingService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<RatingResponse>> getProductRatings(@PathVariable UUID productId) {
        List<RatingResponse> ratings = ratingService.getProductRatings(productId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/average/{productId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID productId) {
        Double averageRating = ratingService.getAverageRatingForProduct(productId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }
} 