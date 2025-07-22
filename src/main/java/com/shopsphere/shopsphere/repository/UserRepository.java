package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    List<User> findByRole(Role role);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}