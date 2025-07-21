package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByEmail(String email);

    boolean existsByEmail(String email);
}