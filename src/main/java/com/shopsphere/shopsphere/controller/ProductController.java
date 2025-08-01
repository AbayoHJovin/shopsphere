package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.ProductCreateRequest;
import com.shopsphere.shopsphere.dto.request.ProductSearchFilterRequest;
import com.shopsphere.shopsphere.dto.request.ProductUpdateRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.dto.response.ProductResponse;
import com.shopsphere.shopsphere.enums.Gender;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "stock", defaultValue = "0") Integer stock,
            @RequestParam(value = "gender", required = false) Gender gender,
            @RequestParam(value = "popularity", defaultValue = "false") Boolean popular,
            @RequestParam(value = "categories", required = false) List<UUID> categoryIds,
            @RequestParam(value = "colors", required = false) List<UUID> colorIds,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest servletRequest) {
        try {
            log.info("Creating new product: {}", name);
            
            // Build ProductCreateRequest from individual parameters
            ProductCreateRequest request = ProductCreateRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .popular(popular)
                .categoryIds(categoryIds != null ? categoryIds : new ArrayList<>())
                .colorIds(colorIds != null ? colorIds : new ArrayList<>())
                .build();
                
            // Set gender if provided
            if (gender != null) {
                request.setGender(gender);
            }
            
            ProductResponse response = productService.createProduct(request, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> updateProductJson(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating product with ID: {}", productId);
            ProductResponse response = productService.updateProduct(productId, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
    
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID productId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "stock", defaultValue = "0") Integer stock,
            @RequestParam(value = "gender", required = false) Gender gender,
            @RequestParam(value = "popularity", required = false) Boolean popular,
            @RequestParam(value = "categories", required = false) List<UUID> categoryIds,
            @RequestParam(value = "colors", required = false) List<UUID> colorIds,
            @RequestParam(value = "previousPrice", required = false) BigDecimal previousPrice,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating product with ID: {} using multipart form data", productId);
            
            // Build ProductUpdateRequest from individual parameters
            ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .popular(popular) // Using 'popularity' from frontend as 'popular' in backend
                .categoryIds(categoryIds != null ? categoryIds : new ArrayList<>())
                .colorIds(colorIds != null ? colorIds : new ArrayList<>())
                .build();
                
            // Set gender if provided
            if (gender != null) {
                request.setGender(gender);
            }
            
            // Update the product details
            ProductResponse response = productService.updateProduct(productId, request);
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable UUID productId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching product with ID: {}", productId);
            ProductResponse response = productService.getProductById(productId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all products with pagination");
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductResponse> response = productService.getAllProducts(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching products: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Searching products with query: {}", query);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> response = productService.searchProducts(query, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error searching products: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
    
    @PostMapping("/advanced-search")
    public ResponseEntity<?> advancedSearchProducts(
            @RequestBody ProductSearchFilterRequest filter,
            HttpServletRequest servletRequest) {
        try {
            log.info("Performing advanced search with filters: {}", filter);
            Page<ProductResponse> response = productService.advancedSearchProducts(filter);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during advanced search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error during advanced search: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {
        try {
            log.info("Fetching products by category ID: {}", categoryId);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> response = productService.getProductsByCategory(categoryId, pageable);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error fetching products by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching products by category: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> deleteProduct(
            @PathVariable UUID productId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting product with ID: {}", productId);
            productService.deleteProduct(productId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Product deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting product: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> addProductImages(
            @PathVariable UUID productId,
            @RequestPart("images") List<MultipartFile> images,
            HttpServletRequest servletRequest) {
        try {
            log.info("Adding images to product with ID: {}", productId);
            ProductResponse response = productService.addProductImages(productId, images);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error adding product images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error adding product images: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting image with ID: {} from product with ID: {}", imageId, productId);
            productService.deleteProductImage(productId, imageId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Product image deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Product or image not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting product image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting product image: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PutMapping("/{productId}/images/{imageId}/main")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> setMainProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Setting image with ID: {} as main for product with ID: {}", imageId, productId);
            ProductResponse response = productService.setMainProductImage(productId, imageId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product or image not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error setting main product image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error setting main product image: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'CO_WORKER')")
    public ResponseEntity<?> updateProductStock(
            @PathVariable UUID productId,
            @RequestParam Integer quantity,
            HttpServletRequest servletRequest) {
        try {
            log.info("Updating stock for product with ID: {} to {}", productId, quantity);
            ProductResponse response = productService.updateProductStock(productId, quantity);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid quantity", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error updating product stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating product stock: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
}