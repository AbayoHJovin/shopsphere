package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.ProductCreateRequest;
import com.shopsphere.shopsphere.dto.request.ProductSearchFilterRequest;
import com.shopsphere.shopsphere.dto.request.ProductUpdateRequest;
import com.shopsphere.shopsphere.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> images);
    ProductResponse updateProduct(UUID productId, ProductUpdateRequest request);
    ProductResponse getProductById(UUID productId);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(String query, Pageable pageable);
    Page<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable);
    void deleteProduct(UUID productId);
    ProductResponse addProductImages(UUID productId, List<MultipartFile> images);
    void deleteProductImage(UUID productId, UUID imageId);
    ProductResponse setMainProductImage(UUID productId, UUID imageId);
    ProductResponse updateProductStock(UUID productId, Integer quantity);
    
    // Advanced search with multiple filters
    Page<ProductResponse> advancedSearchProducts(ProductSearchFilterRequest filter);
} 