package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Discount;
import com.shopsphere.shopsphere.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {

    Page<Discount> findByActiveTrue(Pageable pageable);

    @Query("SELECT d FROM Discount d JOIN d.products p WHERE p.productId = :productId")
    Page<Discount> findByProductId(@Param("productId") UUID productId, Pageable pageable);

    @Query("SELECT d FROM Discount d JOIN d.products p WHERE p.productId = :productId AND d.active = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findCurrentDiscountsByProductId(@Param("productId") UUID productId, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM Discount d WHERE d.active = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findCurrentDiscounts(@Param("now") LocalDateTime now);

    List<Discount> findByEndDateBeforeAndActiveTrue(LocalDateTime now);
}