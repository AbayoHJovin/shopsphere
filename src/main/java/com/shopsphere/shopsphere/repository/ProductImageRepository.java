package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductOrderByPositionAsc(Product product);

    Optional<ProductImage> findByProductAndIsMainTrue(Product product);
}