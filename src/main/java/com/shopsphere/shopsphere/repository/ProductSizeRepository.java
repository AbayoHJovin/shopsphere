package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.enums.Size;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, UUID> {
    List<ProductSize> findByProduct(Product product);
    Optional<ProductSize> findByProductAndSize(Product product, Size size);
    void deleteByProduct(Product product);
} 