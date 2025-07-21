package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByName(String name);

    List<Category> findByParentIsNull();

    @Query("SELECT c FROM Category c WHERE c.parent.categoryId = :parentId")
    List<Category> findByParentId(UUID parentId);
}