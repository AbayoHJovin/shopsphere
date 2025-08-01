package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.ProductColorRequest;
import com.shopsphere.shopsphere.dto.response.ProductColorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing product colors
 */
public interface ProductColorService {
    /**
     * Create a new product color
     * @param request Details for the new color
     * @return The created color response
     */
    ProductColorResponse createProductColor(ProductColorRequest request);
    
    /**
     * Update an existing product color
     * @param colorId ID of the color to update
     * @param request New color details
     * @return The updated color response
     */
    ProductColorResponse updateProductColor(UUID colorId, ProductColorRequest request);
    
    /**
     * Delete a product color
     * @param colorId ID of the color to delete
     */
    void deleteProductColor(UUID colorId);
    
    /**
     * Get a product color by ID
     * @param colorId ID of the color to retrieve
     * @return The color response
     */
    ProductColorResponse getProductColorById(UUID colorId);
    
    /**
     * Get colors by a list of IDs
     * @param colorIds List of color IDs to retrieve
     * @return List of color responses
     */
    List<ProductColorResponse> getColorsByIds(List<UUID> colorIds);
    
    /**
     * Get all product colors with pagination
     * @param pageable Pagination information
     * @return Page of color responses
     */
    Page<ProductColorResponse> getAllProductColors(Pageable pageable);
    
    /**
     * Search for product colors by name
     * @param name Color name to search for
     * @param pageable Pagination information
     * @return Page of matching color responses
     */
    Page<ProductColorResponse> searchProductColors(String name, Pageable pageable);
}