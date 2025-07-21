package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.*;
import com.shopsphere.shopsphere.dto.response.*;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.exception.TokenRefreshException;
import com.shopsphere.shopsphere.models.RefreshToken;
import com.shopsphere.shopsphere.models.ResetPasswordToken;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserService userService;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final ResetPasswordService resetPasswordService;
        private final EmailService emailService;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;

        @Override
        @Transactional
        public SignupResponse signup(SignupRequest request) {
                // Check if user already exists
                if (userService.existsByEmail(request.getEmail())) {
                        return SignupResponse.builder()
                                        .message("Email is already in use!")
                                        .build();
                }

                // Create new user
                User user = User.builder()
                                .username(request.getUsername())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.CUSTOMER)
                                .profilePicture(request.getProfilePicture()) // Set profile picture if provided
                                .build();

                User savedUser = userService.save(user);

                // Send welcome email
                emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

                return SignupResponse.builder()
                                .userId(savedUser.getUserId())
                                .username(savedUser.getUsername())
                                .email(savedUser.getEmail())
                                .message("User registered successfully!")
                                .build();
        }

        @Override
        public LoginResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authentication
                                .getPrincipal();

                User user = userService.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with email: " + userDetails.getUsername()));

                Map<String, Object> claims = new HashMap<>();
                claims.put("userId", user.getUserId().toString());
                claims.put("role", user.getRole().name());

                String accessToken = jwtService.generateAccessToken(claims, userDetails);

                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                return LoginResponse.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .accessToken(accessToken)
                                .refreshToken(refreshToken.getToken())
                                .tokenType("Bearer")
                                .build();
        }

        @Override
        @Transactional
        public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
                String requestRefreshToken = request.getRefreshToken();

                return refreshTokenService.findByToken(requestRefreshToken)
                                .map(refreshTokenService::verifyExpiration)
                                .map(RefreshToken::getUser)
                                .map(user -> {
                                        // Load user details
                                        org.springframework.security.core.userdetails.UserDetails userDetails = userService
                                                        .loadUserByUsername(user.getEmail());

                                        // Generate new access token
                                        Map<String, Object> claims = new HashMap<>();
                                        claims.put("userId", user.getUserId().toString());
                                        claims.put("role", user.getRole().name());

                                        String accessToken = jwtService.generateAccessToken(claims, userDetails);

                                        return RefreshTokenResponse.builder()
                                                        .accessToken(accessToken)
                                                        .refreshToken(requestRefreshToken)
                                                        .tokenType("Bearer")
                                                        .build();
                                })
                                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                                                "Refresh token not found!"));
        }

        @Override
        @Transactional
        public MessageResponse logout(LogoutRequest request) {
                refreshTokenService.deleteByToken(request.getRefreshToken());

                return MessageResponse.builder()
                                .message("Logged out successfully!")
                                .success(true)
                                .build();
        }

        @Override
        @Transactional
        public MessageResponse forgotPassword(ForgotPasswordRequest request) {
                return userService.findByEmail(request.getEmail())
                                .map(user -> {
                                        ResetPasswordToken resetToken = resetPasswordService
                                                        .createResetPasswordToken(user);
                                        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());

                                        return MessageResponse.builder()
                                                        .message("Password reset link sent to your email!")
                                                        .success(true)
                                                        .build();
                                })
                                .orElse(MessageResponse.builder()
                                                .message("Email not found!")
                                                .success(false)
                                                .build());
        }

        @Override
        @Transactional
        public MessageResponse resetPassword(ResetPasswordRequest request) {
                return resetPasswordService.findByToken(request.getToken())
                                .map(resetPasswordService::verifyExpiration)
                                .map(resetToken -> {
                                        User user = resetToken.getUser();
                                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                                        userService.update(user);

                                        // Delete the used token
                                        resetPasswordService.deleteByUser(user);

                                        return MessageResponse.builder()
                                                        .message("Password has been reset successfully!")
                                                        .success(true)
                                                        .build();
                                })
                                .orElse(MessageResponse.builder()
                                                .message("Invalid or expired token!")
                                                .success(false)
                                                .build());
        }

        @Override
        @Transactional
        public MessageResponse changePassword(ChangePasswordRequest request) {
                // Get the currently authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                User user = userService.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + userDetails.getUsername()));

                // Verify current password
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new BadCredentialsException("Current password is incorrect");
                }

                // Update to new password
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userService.update(user);

                // Invalidate all refresh tokens for this user to force re-login with new
                // password
                refreshTokenService.deleteByUserId(user);

                return MessageResponse.builder()
                                .message("Password changed successfully")
                                .success(true)
                                .build();
        }

        @Override
        @Transactional
        public MessageResponse changeEmail(ChangeEmailRequest request) {
                // Get the currently authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                User user = userService.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + userDetails.getUsername()));

                // Verify password
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new BadCredentialsException("Password is incorrect");
                }

                // Check if the new email is already in use
                if (userService.existsByEmail(request.getNewEmail())) {
                        return MessageResponse.builder()
                                        .message("Email is already in use")
                                        .success(false)
                                        .build();
                }

                // Update email
                user.setEmail(request.getNewEmail());
                userService.update(user);

                // Invalidate all refresh tokens for this user to force re-login with new email
                refreshTokenService.deleteByUserId(user);

                return MessageResponse.builder()
                                .message("Email changed successfully. Please login again with your new email.")
                                .success(true)
                                .build();
        }

        @Override
        @Transactional
        public UserResponse updateAccount(UpdateAccountRequest request) {
                // Get the currently authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                User user = userService.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + userDetails.getUsername()));

                // Update username if provided
                if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                        user.setUsername(request.getUsername());
                }

                // Update profile picture if provided
                if (request.getProfilePicture() != null) {
                        // The profile picture URL is provided directly in the request
                        // The actual image upload is handled by the ProfileImageController
                        user.setProfilePicture(request.getProfilePicture());
                }

                User updatedUser = userService.update(user);

                return UserResponse.builder()
                                .userId(updatedUser.getUserId())
                                .username(updatedUser.getUsername())
                                .email(updatedUser.getEmail())
                                .role(updatedUser.getRole())
                                .profilePicture(updatedUser.getProfilePicture())
                                .createdAt(updatedUser.getCreatedAt())
                                .updatedAt(updatedUser.getUpdatedAt())
                                .build();
        }

        @Override
        public UserResponse getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() ||
                                "anonymousUser".equals(authentication.getPrincipal().toString())) {
                        throw new UsernameNotFoundException("User not authenticated");
                }

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + userDetails.getUsername()));

                return UserResponse.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .profilePicture(user.getProfilePicture())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .build();
        }
}