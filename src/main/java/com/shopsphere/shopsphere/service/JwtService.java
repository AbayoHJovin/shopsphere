package com.shopsphere.shopsphere.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.UUID;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);

    String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails);

    String extractUsername(String token);

    UUID extractUserId(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}