package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.dto.response.UserResponse;
import com.shopsphere.shopsphere.service.CloudinaryService;
import com.shopsphere.shopsphere.service.UserService;
import com.shopsphere.shopsphere.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileImageController {

    private final CloudinaryService cloudinaryService;
    private final UserService userService;

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile image) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + userDetails.getUsername()));

            // Delete existing profile image if it exists
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(user.getProfilePicture());
                if (publicId != null) {
                    try {
                        cloudinaryService.deleteImage(publicId);
                    } catch (IOException e) {
                        log.warn("Failed to delete existing profile image: {}", publicId, e);
                        // Continue with upload even if deletion fails
                    }
                }
            }

            // Upload new image to Cloudinary
            Map<String, String> uploadResult = cloudinaryService.uploadImage(image);
            String imageUrl = uploadResult.get("secure_url");

            // Update user profile picture
            user.setProfilePicture(imageUrl);
            User updatedUser = userService.update(user);

            // Return updated user data
            UserResponse response = UserResponse.builder()
                    .userId(updatedUser.getUserId())
                    .username(updatedUser.getUsername())
                    .email(updatedUser.getEmail())
                    .role(updatedUser.getRole())
                    .profilePicture(updatedUser.getProfilePicture())
                    .createdAt(updatedUser.getCreatedAt())
                    .updatedAt(updatedUser.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload profile image", e);
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .message("Failed to upload profile image: " + e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error processing profile image upload", e);
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .message("Error processing profile image upload: " + e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @DeleteMapping("/delete-image")
    public ResponseEntity<?> deleteProfileImage() {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + userDetails.getUsername()));

            // Check if user has a profile picture
            if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        MessageResponse.builder()
                                .message("No profile picture to delete")
                                .success(false)
                                .build());
            }

            // Delete profile image from Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(user.getProfilePicture());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }

            // Update user profile
            user.setProfilePicture(null);
            userService.update(user);

            return ResponseEntity.ok(
                    MessageResponse.builder()
                            .message("Profile picture deleted successfully")
                            .success(true)
                            .build());
        } catch (IOException e) {
            log.error("Failed to delete profile image", e);
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .message("Failed to delete profile image: " + e.getMessage())
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error processing profile image deletion", e);
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .message("Error processing profile image deletion: " + e.getMessage())
                            .success(false)
                            .build());
        }
    }
}