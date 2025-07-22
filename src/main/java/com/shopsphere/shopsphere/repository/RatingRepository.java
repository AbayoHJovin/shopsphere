package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.Rating;
import com.shopsphere.shopsphere.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByProductOrderByCreatedAtDesc(Product product);
    List<Rating> findByUser(User user);
    Optional<Rating> findByProductAndUser(Product product, User user);
    
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.product = ?1")
    Double findAverageRatingByProduct(Product product);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.product = ?1")
    Integer countRatingsByProduct(Product product);
} 