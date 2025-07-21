package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.ResetPasswordToken;
import com.shopsphere.shopsphere.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {
    Optional<ResetPasswordToken> findByToken(String token);

    Optional<ResetPasswordToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM ResetPasswordToken rpt WHERE rpt.user = :user")
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM ResetPasswordToken rpt WHERE rpt.expiryDate < :now")
    void deleteAllExpiredTokens(LocalDateTime now);
}