package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.ProductColorRequest;
import com.shopsphere.shopsphere.dto.response.ProductColorResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.ProductColor;
import com.shopsphere.shopsphere.repository.ProductColorRepository;
import com.shopsphere.shopsphere.service.ProductColorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductColorServiceImpl implements ProductColorService {

    private final ProductColorRepository productColorRepository;

    @Override
    @Transactional
    public ProductColorResponse createProductColor(ProductColorRequest request) {
        log.info("Creating product color: {}", request.getColorName());
        
        // Check if color already exists
        productColorRepository.findByColorNameIgnoreCase(request.getColorName())
                .ifPresent(existingColor -> {
                    throw new IllegalArgumentException("Color '" + request.getColorName() + "' already exists");
                });
        
        // Create color entity
        ProductColor color = ProductColor.builder()
                .colorName(request.getColorName())
                .colorHexCode(request.getColorHexCode())
                .build();
        
        // Save color
        ProductColor savedColor = productColorRepository.save(color);
        
        return mapToResponse(savedColor);
    }

    @Override
    @Transactional
    public ProductColorResponse updateProductColor(UUID colorId, ProductColorRequest request) {
        log.info("Updating product color with ID: {}", colorId);
        
        // Find color
        ProductColor color = productColorRepository.findById(colorId)
                .orElseThrow(() -> new ResourceNotFoundException("Product color not found with ID: " + colorId));
        
        // Check if new color name already exists (if name is being changed)
        if (!color.getColorName().equalsIgnoreCase(request.getColorName())) {
            productColorRepository.findByColorNameIgnoreCase(request.getColorName())
                    .ifPresent(existingColor -> {
                        throw new IllegalArgumentException("Color '" + request.getColorName() + "' already exists");
                    });
        }
        
        // Update color entity
        color.setColorName(request.getColorName());
        color.setColorHexCode(request.getColorHexCode());
        
        // Save updated color
        ProductColor updatedColor = productColorRepository.save(color);
        
        return mapToResponse(updatedColor);
    }

    @Override
    @Transactional
    public void deleteProductColor(UUID colorId) {
        log.info("Deleting product color with ID: {}", colorId);
        
        // Find color
        ProductColor color = productColorRepository.findById(colorId)
                .orElseThrow(() -> new ResourceNotFoundException("Product color not found with ID: " + colorId));
        
        // Delete color
        productColorRepository.delete(color);
    }

    @Override
    public ProductColorResponse getProductColorById(UUID colorId) {
        log.info("Fetching product color with ID: {}", colorId);
        
        // Find color
        ProductColor color = productColorRepository.findById(colorId)
                .orElseThrow(() -> new ResourceNotFoundException("Product color not found with ID: " + colorId));
        
        return mapToResponse(color);
    }

    @Override
    public List<ProductColorResponse> getColorsByIds(List<UUID> colorIds) {
        log.info("Fetching colors by IDs: {}", colorIds);
        
        // Find colors by IDs
        List<ProductColor> colors = productColorRepository.findByColorIdIn(colorIds);
        
        // Check if all requested colors were found
        if (colors.size() != colorIds.size()) {
            log.warn("Not all requested colors were found. Requested: {}, Found: {}", colorIds.size(), colors.size());
        }
        
        // Map to responses
        return colors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductColorResponse> getAllProductColors(Pageable pageable) {
        log.info("Fetching all product colors with pagination");
        
        // Find all colors with pagination
        Page<ProductColor> colors = productColorRepository.findAll(pageable);
        
        // Map to responses
        return colors.map(this::mapToResponse);
    }

    @Override
    public Page<ProductColorResponse> searchProductColors(String name, Pageable pageable) {
        log.info("Searching product colors with name: {}", name);
        
        // Search colors by name
        Page<ProductColor> colors = productColorRepository.findByColorNameContainingIgnoreCase(name, pageable);
        
        // Map to responses
        return colors.map(this::mapToResponse);
    }
    
    /**
     * Maps a ProductColor entity to a ProductColorResponse DTO
     */
    private ProductColorResponse mapToResponse(ProductColor color) {
        return ProductColorResponse.builder()
                .colorId(color.getColorId())
                .colorName(color.getColorName())
                .colorHexCode(color.getColorHexCode())
                .build();
    }
}