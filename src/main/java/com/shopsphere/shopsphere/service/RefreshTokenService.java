package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.models.RefreshToken;
import com.shopsphere.shopsphere.models.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByUserId(User user);

    void deleteByToken(String token);
}