package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.exception.TokenRefreshException;
import com.shopsphere.shopsphere.models.ResetPasswordToken;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.repository.ResetPasswordTokenRepository;
import com.shopsphere.shopsphere.service.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    // Token expiry time: 15 minutes
    private static final long RESET_TOKEN_EXPIRY_MS = 900000;

    @Override
    @Transactional
    public ResetPasswordToken createResetPasswordToken(User user) {
        // Delete any existing token for this user
        resetPasswordTokenRepository.findByUser(user).ifPresent(resetPasswordTokenRepository::delete);

        ResetPasswordToken resetToken = ResetPasswordToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plus(RESET_TOKEN_EXPIRY_MS, ChronoUnit.MILLIS))
                .build();

        return resetPasswordTokenRepository.save(resetToken);
    }

    @Override
    public Optional<ResetPasswordToken> findByToken(String token) {
        return resetPasswordTokenRepository.findByToken(token);
    }

    @Override
    public ResetPasswordToken verifyExpiration(ResetPasswordToken token) {
        if (token.isExpired()) {
            resetPasswordTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Reset password token was expired. Please make a new reset request");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        resetPasswordTokenRepository.deleteByUser(user);
    }
}