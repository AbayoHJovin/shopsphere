package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {
    /**
     * Get the most recently updated configuration
     * This is useful since we typically only have one active configuration
     */
    @Query("SELECT c FROM Configuration c ORDER BY c.updatedAt DESC NULLS LAST, c.createdAt DESC")
    Optional<Configuration> findLatestConfiguration();
} 