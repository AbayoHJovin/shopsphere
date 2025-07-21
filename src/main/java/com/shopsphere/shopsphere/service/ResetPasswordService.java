package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.models.ResetPasswordToken;
import com.shopsphere.shopsphere.models.User;

import java.util.Optional;

public interface ResetPasswordService {
    ResetPasswordToken createResetPasswordToken(User user);

    Optional<ResetPasswordToken> findByToken(String token);

    ResetPasswordToken verifyExpiration(ResetPasswordToken token);

    void deleteByUser(User user);
}