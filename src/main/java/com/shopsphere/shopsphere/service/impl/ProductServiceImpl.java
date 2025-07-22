package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.ProductCreateRequest;
import com.shopsphere.shopsphere.dto.request.ProductUpdateRequest;
import com.shopsphere.shopsphere.dto.request.ProductSearchFilterRequest;
import com.shopsphere.shopsphere.dto.response.CategoryResponse;
import com.shopsphere.shopsphere.dto.response.ProductImageResponse;
import com.shopsphere.shopsphere.dto.response.ProductResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Category;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.ProductImage;
import com.shopsphere.shopsphere.repository.CategoryRepository;
import com.shopsphere.shopsphere.repository.ProductImageRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.repository.DiscountRepository;
import com.shopsphere.shopsphere.service.CloudinaryService;
import com.shopsphere.shopsphere.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.shopsphere.shopsphere.dto.response.ProductDiscountResponse;
import com.shopsphere.shopsphere.models.Discount;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final DiscountRepository discountRepository;

    // Thread pool for parallel image uploads
    private final ExecutorService imageUploadExecutor = Executors.newFixedThreadPool(5);

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> images) {
        log.info("Creating new product: {}", request.getName());

        // Fetch categories
        List<Category> categories = getCategoriesFromIds(request.getCategoryIds());

        // Create product entity
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .gender(request.getGender())
                .stock(request.getStock())
                .popular(request.getPopular())
                .categories(categories)
                .images(new ArrayList<>())
                .build();

        // Save product to get an ID
        Product savedProduct = productRepository.save(product);

        // Upload images in parallel if provided
        if (images != null && !images.isEmpty()) {
            uploadProductImages(savedProduct, images);
        }

        // Fetch the product with all associations
        Product refreshedProduct = productRepository.findById(savedProduct.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapProductToResponse(refreshedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
        log.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Update fields if provided
        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getGender() != null) {
            product.setGender(request.getGender());
        }

        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }

        if (request.getPopular() != null) {
            product.setPopular(request.getPopular());
        }

        // Update categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = getCategoriesFromIds(request.getCategoryIds());
            product.setCategories(categories);
        }

        Product updatedProduct = productRepository.save(product);

        return mapProductToResponse(updatedProduct);
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        log.info("Fetching product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return mapProductToResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");

        return productRepository.findAll(pageable)
                .map(this::mapProductToResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        log.info("Searching products with query: {}", query);

        return productRepository.findByNameContainingIgnoreCase(query, pageable)
                .map(this::mapProductToResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        log.info("Fetching products by category ID: {}", categoryId);

        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapProductToResponse);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        log.info("Deleting product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Delete product images from Cloudinary
        for (ProductImage image : product.getImages()) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(image.getImageUrl());
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            } catch (IOException e) {
                log.error("Failed to delete image from Cloudinary for product ID: {}", productId, e);
                // Continue with deletion even if image deletion fails
            }
        }

        // Delete product from database
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponse addProductImages(UUID productId, List<MultipartFile> images) {
        log.info("Adding images to product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        uploadProductImages(product, images);

        // Fetch the product with all associations
        Product refreshedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapProductToResponse(refreshedProduct);
    }

    @Override
    @Transactional
    public void deleteProductImage(UUID productId, UUID imageId) {
        log.info("Deleting image with ID: {} from product with ID: {}", imageId, productId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with ID: " + imageId));

        // Check if image belongs to the specified product
        if (!image.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }

        // Delete image from Cloudinary
        try {
            String publicId = cloudinaryService.extractPublicIdFromUrl(image.getImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary", e);
            // Continue with deletion even if Cloudinary deletion fails
        }

        // Delete image from database
        productImageRepository.delete(image);
    }

    @Override
    @Transactional
    public ProductResponse setMainProductImage(UUID productId, UUID imageId) {
        log.info("Setting image with ID: {} as main for product with ID: {}", imageId, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        ProductImage newMainImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with ID: " + imageId));

        // Check if image belongs to the specified product
        if (!newMainImage.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }

        // Reset isMain flag for all product images
        for (ProductImage image : product.getImages()) {
            image.setIsMain(false);
            productImageRepository.save(image);
        }

        // Set the new main image
        newMainImage.setIsMain(true);
        productImageRepository.save(newMainImage);

        // Fetch the product with all associations
        Product refreshedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapProductToResponse(refreshedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStock(UUID productId, Integer quantity) {
        log.info("Updating stock for product with ID: {} to {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        product.setStock(quantity);
        Product updatedProduct = productRepository.save(product);

        return mapProductToResponse(updatedProduct);
    }

    @Override
    public Page<ProductResponse> advancedSearchProducts(ProductSearchFilterRequest filter) {
        log.info("Performing advanced search with filters");
        
        Specification<Product> specification = buildSpecificationFromFilter(filter);
        
        Sort sort = buildSortFromFilter(filter);
        
        Pageable pageable = PageRequest.of(
            filter.getPage() != null ? filter.getPage() : 0,
            filter.getSize() != null ? filter.getSize() : 10,
            sort
        );
        
        Page<Product> productPage = productRepository.findAll(specification, pageable);
        
        return productPage.map(this::mapProductToResponse);
    }
    
    private Specification<Product> buildSpecificationFromFilter(ProductSearchFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Text search (keyword)
            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keyword)
                ));
            }
            
            // Price range
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            
            // Categories
            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                Join<Object, Object> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(categoryJoin.get("categoryId").in(filter.getCategoryIds()));
                
                // Ensure distinct results since a product can belong to multiple categories
                query.distinct(true);
            }
            
            // Rating range
            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), filter.getMinRating()));
            }
            
            if (filter.getMaxRating() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("averageRating"), filter.getMaxRating()));
            }
            
            // Stock status
            if (filter.getInStock() != null && filter.getInStock()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("stock"), 0));
            }
            
            // On sale (has discount/previous price)
            if (filter.getOnSale() != null && filter.getOnSale()) {
                predicates.add(criteriaBuilder.isNotNull(root.get("previousPrice")));
            }
            
            // Popular products
            if (filter.getPopular() != null && filter.getPopular()) {
                predicates.add(criteriaBuilder.isTrue(root.get("popular")));
            }
            
            // Gender filter
            if (filter.getGender() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), filter.getGender()));
            }
            
            // New arrivals - we can assume this based on product ID (newer products have higher IDs)
            // This is a simplification; ideally we would have a createdAt field
            if (filter.getNewArrivals() != null && filter.getNewArrivals()) {
                query.orderBy(criteriaBuilder.desc(root.get("productId")));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private Sort buildSortFromFilter(ProductSearchFilterRequest filter) {
        String sortBy = "name"; // Default sort field
        if (filter.getSortBy() != null && !filter.getSortBy().isEmpty()) {
            sortBy = filter.getSortBy();
        }
        
        Sort.Direction direction = Sort.Direction.ASC; // Default sort direction
        if (filter.getSortDirection() != null && filter.getSortDirection().equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        
        return Sort.by(direction, sortBy);
    }

    // Helper methods
    private List<Category> getCategoriesFromIds(List<UUID> categoryIds) {
        return categoryIds.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id)))
                .collect(Collectors.toList());
    }

    private void uploadProductImages(Product product, List<MultipartFile> images) {
        // Check if there are any existing images
        boolean hasExistingImages = !product.getImages().isEmpty();

        // Process images in parallel using CompletableFuture
        List<CompletableFuture<ProductImage>> futures = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            final int position = i;
            final boolean isMain = !hasExistingImages && position == 0; // First image is main if no existing images

            MultipartFile imageFile = images.get(i);

            CompletableFuture<ProductImage> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Upload to Cloudinary
                    Map<String, String> uploadResult = cloudinaryService.uploadImage(imageFile);
                    String imageUrl = uploadResult.get("secure_url");

                    // Create and save ProductImage entity
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imageUrl)
                            .isMain(isMain)
                            .position(position)
                            .build();

                    return productImageRepository.save(productImage);
                } catch (IOException e) {
                    log.error("Failed to upload image to Cloudinary", e);
                    throw new RuntimeException("Failed to upload image", e);
                }
            }, imageUploadExecutor);

            futures.add(future);
        }

        // Wait for all uploads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private ProductResponse mapProductToResponse(Product product) {
        List<ProductImageResponse> imageResponses = product.getImages().stream()
                .map(image -> ProductImageResponse.builder()
                        .imageId(image.getImageId())
                        .imageUrl(image.getImageUrl())
                        .isMain(image.getIsMain())
                        .position(image.getPosition())
                        .build())
                .sorted(Comparator.comparing(ProductImageResponse::getPosition))
                .collect(Collectors.toList());

        List<CategoryResponse> categoryResponses = product.getCategories().stream()
                .map(category -> CategoryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                        .parentName(category.getParent() != null ? category.getParent().getName() : null)
                        .hasSubcategories(!category.getSubcategories().isEmpty())
                        .subcategoryCount(category.getSubcategories().size())
                        .productCount(category.getProducts().size())
                        .build())
                .collect(Collectors.toList());

        // Find main image URL
        String mainImageUrl = product.getImages().stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
                
        // Get discounts for this product
        List<Discount> allDiscounts = product.getDiscounts();
        LocalDateTime now = LocalDateTime.now();
        
        // Map discounts to response objects
        List<ProductDiscountResponse> discountResponses = allDiscounts.stream()
                .map(discount -> {
                    boolean isCurrent = discount.isActive() && 
                            discount.getStartDate().isBefore(now) && 
                            discount.getEndDate().isAfter(now);
                            
                    return ProductDiscountResponse.builder()
                            .discountId(discount.getDiscountId())
                            .name(discount.getName())
                            .percentage(discount.getPercentage())
                            .startDate(discount.getStartDate())
                            .endDate(discount.getEndDate())
                            .active(discount.isActive())
                            .current(isCurrent)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Find the currently active discount (if any)
        ProductDiscountResponse activeDiscount = discountResponses.stream()
                .filter(ProductDiscountResponse::isCurrent)
                .findFirst()
                .orElse(null);
        
        // Calculate discounted price if there's an active discount
        BigDecimal discountedPrice = null;
        if (activeDiscount != null && product.getPrice() != null) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    activeDiscount.getPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            discountedPrice = product.getPrice().multiply(discountMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .previousPrice(product.getPreviousPrice())
                .gender(product.getGender())
                .stock(product.getStock())
                .popular(product.getPopular())
                .images(imageResponses)
                .categories(categoryResponses)
                .mainImage(mainImageUrl)
                .averageRating(product.getAverageRating())
                .ratingCount(product.getRatingCount())
                .discounts(discountResponses)
                .activeDiscount(activeDiscount)
                .discountedPrice(discountedPrice)
                .onSale(activeDiscount != null)
                .build();
    }
}