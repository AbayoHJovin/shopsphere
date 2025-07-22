package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.AdminInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminInvitationRepository extends JpaRepository<AdminInvitation, UUID> {

    Optional<AdminInvitation> findByToken(String token);

    Optional<AdminInvitation> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);
}