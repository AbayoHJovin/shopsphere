package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.ProductColor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, UUID> {
    /**
     * Find colors by name (case insensitive, containing)
     */
    Page<ProductColor> findByColorNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find a color by name (for checking duplicates)
     */
    Optional<ProductColor> findByColorNameIgnoreCase(String colorName);
    
    /**
     * Find colors by a list of IDs
     */
    List<ProductColor> findByColorIdIn(List<UUID> colorIds);
}