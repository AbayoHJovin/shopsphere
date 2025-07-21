package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Cart;
import com.shopsphere.shopsphere.models.CartProduct;
import com.shopsphere.shopsphere.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, UUID> {
    List<CartProduct> findByCart(Cart cart);

    Optional<CartProduct> findByCartAndProduct(Cart cart, Product product);

    void deleteByCartAndProduct(Cart cart, Product product);
}