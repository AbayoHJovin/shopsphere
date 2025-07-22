package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.RatingCreateRequest;
import com.shopsphere.shopsphere.dto.response.RatingResponse;
import com.shopsphere.shopsphere.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<RatingResponse> createRating(
            @RequestBody RatingCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        RatingResponse rating = ratingService.createRating(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<RatingResponse>> getProductRatings(@PathVariable UUID productId) {
        List<RatingResponse> ratings = ratingService.getProductRatings(productId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<RatingResponse>> getCurrentUserRatings(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<RatingResponse> ratings = ratingService.getUserRatings(userId);
        return ResponseEntity.ok(ratings);
    }

    @PutMapping("/{ratingId}")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable UUID ratingId,
            @RequestBody RatingCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        RatingResponse rating = ratingService.updateRating(ratingId, request, userId);
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/{ratingId}")
    @PreAuthorize("hasRole('USER') or hasRole('COWORKER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRating(
            @PathVariable UUID ratingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ratingService.deleteRating(ratingId, userId);
        return ResponseEntity.noContent().build();
    }
} 