package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private UUID userId;
    private String username;
    private String email;
    private Role role;
    private String profilePicture; // Changed from profilePictureUrl to match the method called in code
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // Added to match the User model and usage in code
    private boolean active;
}