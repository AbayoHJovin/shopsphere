package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {
    Optional<Discount> findByCode(String code);

    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findActiveDiscounts(LocalDateTime now);

    @Query("SELECT d FROM Discount d JOIN d.products p WHERE p.productId = :productId AND d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findActiveDiscountsByProductId(UUID productId, LocalDateTime now);

    @Query("SELECT d FROM Discount d JOIN d.categories c WHERE c.categoryId = :categoryId AND d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findActiveDiscountsByCategoryId(UUID categoryId, LocalDateTime now);
}