package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private UUID ratingId;
    private Integer stars;
    private String comment;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String username;
    private String userProfilePicture;
    private LocalDateTime createdAt;
    private Boolean verifiedPurchase;
} 