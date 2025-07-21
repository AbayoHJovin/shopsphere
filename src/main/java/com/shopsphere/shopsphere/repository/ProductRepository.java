package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.Gender;
import com.shopsphere.shopsphere.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByGender(Gender gender, Pageable pageable);

    Page<Product> findByPopularTrue(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId")
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    List<Product> findByStockLessThan(int stockThreshold);
}