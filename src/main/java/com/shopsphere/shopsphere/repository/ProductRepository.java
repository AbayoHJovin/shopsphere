package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.Gender;
import com.shopsphere.shopsphere.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId IN :categoryIds")
    Page<Product> findByCategoryIdIn(@Param("categoryIds") List<UUID> categoryIds, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.discounts d WHERE d.active = true")
    Page<Product> findByDiscountsActiveTrue(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.previousPrice IS NOT NULL")
    Page<Product> findByPreviousPriceIsNotNull(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.averageRating >= :minRating AND p.averageRating <= :maxRating")
    Page<Product> findByAverageRatingBetween(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price >= :minPrice AND p.price <= :maxPrice")
    Page<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
    
    Page<Product> findByGender(Gender gender, Pageable pageable);
    
    Page<Product> findByPopularTrue(Pageable pageable);
    
    Page<Product> findByStockGreaterThan(Integer stock, Pageable pageable);

    long countByStockEquals(int stock);

    long countByStockLessThan(int stockThreshold);
}