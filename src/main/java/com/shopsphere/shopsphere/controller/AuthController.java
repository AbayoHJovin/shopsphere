package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.*;
import com.shopsphere.shopsphere.dto.response.*;
import com.shopsphere.shopsphere.service.AuthService;
import com.shopsphere.shopsphere.service.JwtService;
import com.shopsphere.shopsphere.service.RefreshTokenService;
import com.shopsphere.shopsphere.service.CloudinaryService;
import com.shopsphere.shopsphere.models.RefreshToken;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.exception.TokenRefreshException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CloudinaryService cloudinaryService;

    @Value("${app.jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping(value = "/signup-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignupResponse> signupWithImage(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            // Create signup request
            SignupRequest request = SignupRequest.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .build();
 
            // Upload image to Cloudinary if provided
            if (image != null && !image.isEmpty()) {
                Map<String, String> uploadResult = cloudinaryService.uploadImage(image);
                String imageUrl = uploadResult.get("secure_url");
                request.setProfilePicture(imageUrl);
            }

            // Process signup
            return ResponseEntity.ok(authService.signup(request));
        } catch (IOException e) {
            // Handle image upload error
            return ResponseEntity.badRequest().body(
                    SignupResponse.builder()
                            .message("Failed to upload profile image: " + e.getMessage())
                            .build());
        } catch (Exception e) {
            // Handle other errors
            return ResponseEntity.badRequest().body(
                    SignupResponse.builder()
                            .message("Error during signup: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        // Set access token cookie
        Cookie accessTokenCookie = new Cookie("access_token", loginResponse.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // Set to true in production
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (accessTokenExpirationMs / 1000)); // Convert ms to seconds
        response.addCookie(accessTokenCookie);

        // Set refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Set to true in production
        refreshTokenCookie.setPath("/api/auth/refresh-token"); // Restrict to refresh endpoint
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpirationMs / 1000)); // Convert ms to seconds
        response.addCookie(refreshTokenCookie);

        // Remove tokens from response body
        loginResponse.setAccessToken(null);
        loginResponse.setRefreshToken(null);
        loginResponse.setTokenType(null);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = extractCookieValue(request, "refresh_token");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    MessageResponse.builder()
                            .message("Refresh token is missing")
                            .success(false)
                            .build());
        }

        try {
            // Verify and get a new access token
            return refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(token -> {
                        User user = token.getUser();
                        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                                .username(user.getEmail())
                                .password(user.getPassword())
                                .authorities("ROLE_" + user.getRole().name())
                                .build();

                        // Generate new access token
                        Map<String, Object> claims = new HashMap<>();
                        claims.put("userId", user.getUserId().toString());
                        claims.put("role", user.getRole().name());

                        String newAccessToken = jwtService.generateAccessToken(claims, userDetails);

                        // Set new access token cookie
                        Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
                        accessTokenCookie.setHttpOnly(true);
                        accessTokenCookie.setSecure(true); // Set to true in production
                        accessTokenCookie.setPath("/");
                        accessTokenCookie.setMaxAge((int) (accessTokenExpirationMs / 1000)); // Convert ms to seconds
                        response.addCookie(accessTokenCookie);

                        return ResponseEntity.ok(
                                MessageResponse.builder()
                                        .message("Token refreshed successfully")
                                        .success(true)
                                        .build());
                    })
                    .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token not found!"));
        } catch (TokenRefreshException e) {
            // Clear cookies on error
            clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    MessageResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = extractCookieValue(request, "refresh_token");

        if (refreshToken != null) {
            // Delete the refresh token from database
            refreshTokenService.deleteByToken(refreshToken);
        }

        // Clear auth cookies
        clearAuthCookies(response);

        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Logged out successfully")
                        .success(true)
                        .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PutMapping("/update-account")
    public ResponseEntity<UserResponse> updateAccount(@Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(authService.updateAccount(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response) {
        MessageResponse messageResponse = authService.changePassword(request);

        // If password change was successful, clear auth cookies to force re-login
        if (messageResponse.isSuccess()) {
            clearAuthCookies(response);
        }

        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/change-email")
    public ResponseEntity<MessageResponse> changeEmail(@Valid @RequestBody ChangeEmailRequest request,
            HttpServletResponse response) {
        MessageResponse messageResponse = authService.changeEmail(request);

        // If email change was successful, clear auth cookies to force re-login
        if (messageResponse.isSuccess()) {
            clearAuthCookies(response);
        }

        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            return ResponseEntity.ok(authService.getCurrentUser());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Helper methods
    private String extractCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies)
                    .filter(c -> name.equals(c.getName()))
                    .findFirst();
            return cookie.map(Cookie::getValue).orElse(null);
        }
        return null;
    }

    private void clearAuthCookies(HttpServletResponse response) {
        // Clear access token cookie
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        // Clear refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/api/auth/refresh-token");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
}